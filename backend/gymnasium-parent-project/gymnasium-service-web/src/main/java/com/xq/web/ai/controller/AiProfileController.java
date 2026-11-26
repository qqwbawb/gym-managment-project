package com.xq.web.ai.controller;

import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.ai.entity.AiProfileParam;
import com.xq.web.ai.entity.AiUserProfile;
import com.xq.web.ai.service.AiProfileService;
import com.xq.web.ai.service.CurrentMemberResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 用户画像接口。
 *
 * 安全约定：所有接口都不接受 memberId 参数，会员身份一律从登录态解析。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/profile")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('sys:aiCoach','sys:aiProfile','sys:aiPlanList')")
public class AiProfileController {

    private final AiProfileService profileService;
    private final CurrentMemberResolver memberResolver;

    /**
     * 查询我的画像。没有画像时 data 为 null，前端据此引导用户去填表。
     */
    @GetMapping
    public ResultVo getMyProfile() {
        Integer memberId;
        try {
            memberId = memberResolver.requireMemberId();
        } catch (IllegalStateException e) {
            return ResultUtils.error(e.getMessage());
        }
        AiUserProfile profile = profileService.getByMemberId(memberId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("profile", profile);
        data.put("configured", profile != null);
        return ResultUtils.success("查询成功", data);
    }

    /**
     * 保存或更新我的画像。
     */
    @PostMapping
    public ResultVo saveMyProfile(@RequestBody AiProfileParam param) {
        Integer memberId;
        try {
            memberId = memberResolver.requireMemberId();
        } catch (IllegalStateException e) {
            return ResultUtils.error(e.getMessage());
        }
        try {
            profileService.saveOrUpdate(memberId, param);
            return ResultUtils.success("保存成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    /**
     * 表单选项字典。
     *
     * 把枚举集中在这里返回，前端不再硬编码；
     * 后端校验用的也是同一套枚举（AiProfileService），避免前后端不一致。
     */
    @GetMapping("/options")
    public ResultVo options() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("goals", optionList(
                "LOSE_FAT", "减脂", "BUILD_MUSCLE", "增肌", "ENDURANCE", "提升耐力", "SHAPE", "塑形"));
        data.put("levels", optionList(
                "BEGINNER", "初级", "INTERMEDIATE", "中级", "ADVANCED", "高级"));
        data.put("equipments", optionList(
                "BARBELL", "杠铃", "DUMBBELL", "哑铃", "MACHINE", "固定器械",
                "CABLE", "龙门架/绳索", "BODYWEIGHT", "自重训练", "CARDIO", "有氧器械"));
        data.put("injuries", optionList(
                "KNEE", "膝关节", "SHOULDER", "肩关节", "LOWER_BACK", "腰/下背",
                "ELBOW", "肘关节", "WRIST", "腕关节", "ANKLE", "踝关节", "NECK", "颈椎"));
        return ResultUtils.success("查询成功", data);
    }

    /**
     * 把 (value, label, value, label, ...) 转成 [{value,label}] 列表。
     */
    private List<Map<String, String>> optionList(String... pairs) {
        List<Map<String, String>> list = new java.util.ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("value", pairs[i]);
            item.put("label", pairs[i + 1]);
            list.add(item);
        }
        return list;
    }
}
