package com.xq.web.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xq.web.ai.entity.AiTrainingLog;
import com.xq.web.ai.entity.AiTrainingLogParam;
import com.xq.web.ai.mapper.AiTrainingLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 训练记录服务（记忆第二层）。
 *
 * 为什么需要它：
 *   训练记录是「AI 有记忆」的实证——没有记录，Prompt 里的
 *   「近期训练记录」永远是空的，模型只能凭空排计划，与「通用模板」没有区别。
 *
 * 数据从哪来：会员在计划详情里点「完成今天训练」，主动打卡。
 *   没有用定时任务自动生成——那会造出用户根本没练过的假数据，
 *   记忆变成幻觉，比没有记忆更糟。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTrainingLogService {

    private final AiTrainingLogMapper trainingLogMapper;

    /** 合法训练部位 */
    private static final Set<String> BODY_PARTS = new HashSet<>(Arrays.asList(
            "CHEST", "BACK", "LEG", "SHOULDER", "ARM", "CORE", "CARDIO"));

    /**
     * 打卡一条训练记录。
     *
     * @param memberId 取自登录态
     */
    @Transactional(rollbackFor = Exception.class)
    public void addLog(Integer memberId, AiTrainingLogParam param) {
        if (param == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(param.getBodyPart()) || !BODY_PARTS.contains(param.getBodyPart())) {
            throw new IllegalArgumentException("训练部位不合法");
        }
        if (param.getDurationMin() != null
                && (param.getDurationMin() < 1 || param.getDurationMin() > 600)) {
            throw new IllegalArgumentException("训练时长需在 1-600 分钟之间");
        }

        AiTrainingLog log = new AiTrainingLog();
        log.setMemberId(memberId);
        log.setPlanId(param.getPlanId());
        log.setTrainDate(parseDate(param.getTrainDate()));
        log.setBodyPart(param.getBodyPart());
        log.setExerciseSummary(truncate(param.getExerciseSummary(), 500));
        log.setDurationMin(param.getDurationMin());

        // 去重：同一计划同一天只能打卡一次。
        // 不去重的话，连点两次或网络重试就会产生两条同日同部位记录，
        // 拼进 Prompt 后看起来像「这个部位练了两遍」，直接影响下一次排计划。
        // 只对带 planId 的打卡去重；手动打卡（planId 为空）允许一天多条不同部位。
        if (log.getPlanId() != null) {
            // 注意：train_date 是 DATE 列，而 parseDate 在「前端没传日期」时返回的是
            // new Date()（带时分秒）。直接 eq 会生成 train_date = '2026-11-26 19:30:00'，
            // MySQL 把这个 DATE 值当成 00:00:00 比较，永远不相等，查重就形同虚设。
            // 所以用「当天 0 点 <= x < 次日 0 点」的区间来圈定同一天。
            Date dayStart = startOfDay(log.getTrainDate());
            Date nextDayStart = new Date(dayStart.getTime() + 24L * 60 * 60 * 1000);

            LambdaQueryWrapper<AiTrainingLog> dup = new LambdaQueryWrapper<>();
            dup.eq(AiTrainingLog::getMemberId, memberId)
                    .eq(AiTrainingLog::getPlanId, log.getPlanId())
                    .ge(AiTrainingLog::getTrainDate, dayStart)
                    .lt(AiTrainingLog::getTrainDate, nextDayStart);
            Long count = trainingLogMapper.selectCount(dup);
            if (count != null && count > 0) {
                throw new IllegalArgumentException("这条训练今天已经打过卡了，不用重复记录");
            }
            // 落库时也统一成当天 0 点，避免 DATE 列按服务器时区换算时出现跨天
            log.setTrainDate(dayStart);
        }

        try {
            trainingLogMapper.insert(log);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发下两次请求同时通过上面的查询检查，由唯一索引兜底
            throw new IllegalArgumentException("这条训练今天已经打过卡了，不用重复记录");
        }
    }

    /**
     * 查某会员最近的训练记录（前端「训练记录」列表用）。
     */
    public List<AiTrainingLog> listRecent(Integer memberId, int limit) {
        LambdaQueryWrapper<AiTrainingLog> query = new LambdaQueryWrapper<>();
        query.eq(AiTrainingLog::getMemberId, memberId)
                .orderByDesc(AiTrainingLog::getTrainDate)
                .orderByDesc(AiTrainingLog::getLogId)
                .last("limit " + Math.min(Math.max(limit, 1), 50));
        List<AiTrainingLog> list = trainingLogMapper.selectList(query);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 删除自己的一条记录（防止误打卡导致记忆被污染）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLog(Integer memberId, Integer logId) {
        LambdaQueryWrapper<AiTrainingLog> query = new LambdaQueryWrapper<>();
        query.eq(AiTrainingLog::getLogId, logId)
                .eq(AiTrainingLog::getMemberId, memberId);
        // 带上 memberId 条件，防止删别人的记录
        int rows = trainingLogMapper.delete(query);
        if (rows == 0) {
            throw new IllegalArgumentException("记录不存在或无权删除");
        }
    }

    private Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return new Date();
        }
        Date parsed;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            parsed = sdf.parse(dateStr.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd");
        }
        // 拒绝未来日期：打卡是记录「已经练完」的事，未来日期会污染 AI 的近期训练记忆，
        // 让模型误以为该部位刚练过（而实际还没练），进而排错计划。
        // 用「今天 23:59:59」作上界，避免同一天因时分秒差异被误判。
        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 23);
        endOfToday.set(Calendar.MINUTE, 59);
        endOfToday.set(Calendar.SECOND, 59);
        endOfToday.set(Calendar.MILLISECOND, 999);
        if (parsed.after(endOfToday.getTime())) {
            throw new IllegalArgumentException("不能为未来日期打卡");
        }
        return parsed;
    }

    /**
     * 取某个时间戳「当天 0 点」。
     *
     * 为什么统一成 0 点：train_date 是 DATE 列，只关心日期。
     * 带时分秒的值在 DATE 列上按服务器时区换算时可能跨天，统一截断最稳。
     */
    private Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date == null ? new Date() : date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
