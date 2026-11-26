package com.xq.web.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xq.web.ai.entity.AiProfileParam;
import com.xq.web.ai.entity.AiUserProfile;
import com.xq.web.ai.mapper.AiUserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户画像服务。
 *
 * 画像属于「事实数据」：由会员填写，AI 只能读不能写。
 * 这是三层记忆里的第一层，也是最稳定的一层。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProfileService {

    private final AiUserProfileMapper profileMapper;

    /** 合法的目标枚举 */
    private static final Set<String> GOALS =
            new HashSet<>(Arrays.asList("LOSE_FAT", "BUILD_MUSCLE", "ENDURANCE", "SHAPE"));

    /** 合法的水平枚举 */
    private static final Set<String> LEVELS =
            new HashSet<>(Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED"));

    /** 合法的伤病部位枚举 */
    private static final Set<String> INJURIES =
            new HashSet<>(Arrays.asList("KNEE", "SHOULDER", "LOWER_BACK", "ELBOW", "WRIST", "ANKLE", "NECK"));

    /** 合法的器械枚举 */
    private static final Set<String> EQUIPMENTS =
            new HashSet<>(Arrays.asList("BARBELL", "DUMBBELL", "MACHINE", "CABLE", "BODYWEIGHT", "CARDIO"));

    /**
     * 查询某会员的画像，没有返回 null。
     */
    public AiUserProfile getByMemberId(Integer memberId) {
        LambdaQueryWrapper<AiUserProfile> query = new LambdaQueryWrapper<>();
        query.eq(AiUserProfile::getMemberId, memberId);
        return profileMapper.selectOne(query);
    }

    /**
     * 保存或更新画像（一个会员一条）。
     *
     * @param memberId 取自登录态
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Integer memberId, AiProfileParam param) {
        validate(param);

        AiUserProfile existing = getByMemberId(memberId);
        AiUserProfile profile = existing == null ? new AiUserProfile() : existing;
        profile.setMemberId(memberId);
        profile.setGoal(param.getGoal());
        profile.setLevel(param.getLevel());
        profile.setDaysPerWeek(param.getDaysPerWeek());
        profile.setSessionMinutes(param.getSessionMinutes());
        profile.setEquipment(joinCsv(param.getEquipment()));
        profile.setInjuries(joinCsv(param.getInjuries()));
        profile.setInjuryNote(param.getInjuryNote());

        if (existing != null) {
            profile.setProfileId(existing.getProfileId());
            profileMapper.updateById(profile);
            return;
        }

        try {
            profileMapper.insert(profile);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发首次保存：连点两次「保存」时，两个请求都查到 existing==null，
            // 都会走 insert，第二个被唯一索引 uk_ai_profile_member 拦下。
            // 这里转成 update，用户看到的是「保存成功」而不是 500。
            AiUserProfile concurrent = getByMemberId(memberId);
            if (concurrent == null) {
                throw e;
            }
            profile.setProfileId(concurrent.getProfileId());
            profileMapper.updateById(profile);
        }
    }

    /**
     * 参数校验：挡住非法枚举与明显不合理的数值。
     *
     * 为什么要在后端校验：前端下拉框可以被绕过（改请求包即可），
     * 非法枚举会直接进 Prompt，污染模型输入。
     */
    private void validate(AiProfileParam param) {
        if (param == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(param.getGoal()) || !GOALS.contains(param.getGoal())) {
            throw new IllegalArgumentException("训练目标不合法");
        }
        if (StringUtils.isBlank(param.getLevel()) || !LEVELS.contains(param.getLevel())) {
            throw new IllegalArgumentException("训练水平不合法");
        }
        if (param.getDaysPerWeek() == null || param.getDaysPerWeek() < 1 || param.getDaysPerWeek() > 7) {
            throw new IllegalArgumentException("每周训练次数需在 1-7 之间");
        }
        if (param.getSessionMinutes() == null || param.getSessionMinutes() < 10 || param.getSessionMinutes() > 240) {
            throw new IllegalArgumentException("单次训练时长需在 10-240 分钟之间");
        }
        if (param.getEquipment() == null || param.getEquipment().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一种可用器械（自重也算）");
        }
        for (String eq : param.getEquipment()) {
            if (!EQUIPMENTS.contains(eq)) {
                throw new IllegalArgumentException("器械类型不合法：" + eq);
            }
        }
        if (param.getInjuries() != null) {
            for (String injury : param.getInjuries()) {
                if (!INJURIES.contains(injury)) {
                    throw new IllegalArgumentException("伤病部位不合法：" + injury);
                }
            }
        }
        if (param.getInjuryNote() != null && param.getInjuryNote().length() > 255) {
            throw new IllegalArgumentException("伤病说明不能超过 255 字");
        }
    }

    private String joinCsv(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }
}
