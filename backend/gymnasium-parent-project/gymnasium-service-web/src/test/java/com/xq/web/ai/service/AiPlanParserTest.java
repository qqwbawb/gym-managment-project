package com.xq.web.ai.service;

import com.xq.web.ai.entity.AiPlanResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JSON 解析器单元测试（第二道防线）。
 *
 * 覆盖的都是线上真实见过的模型畸形输出——
 * 每一条都是「不加防护就会让整次生成失败」的场景。
 */
class AiPlanParserTest {

    private static final String VALID_JSON = "{"
            + "\"title\":\"减脂周计划\","
            + "\"days\":[{\"day\":1,\"bodyPart\":\"chest\","
            + "\"exercises\":[{\"name\":\"哑铃卧推\",\"sets\":4,\"reps\":\"8-12\",\"restSec\":90}]}],"
            + "\"notes\":\"注意热身\"}";

    @Test
    @DisplayName("标准 JSON 能正确解析，bodyPart 统一转大写")
    void parsesPlainJson() {
        AiPlanResult result = AiPlanParser.parse(VALID_JSON);

        assertNotNull(result);
        assertEquals("减脂周计划", result.getTitle());
        assertEquals(1, result.getDays().size());
        assertEquals("CHEST", result.getDays().get(0).getBodyPart(), "bodyPart 应被转为大写");
        assertEquals("哑铃卧推", result.getDays().get(0).getExercises().get(0).getName());
        assertEquals(4, result.getDays().get(0).getExercises().get(0).getSets());
    }

    @Test
    @DisplayName("被 ```json 代码块包裹时也能清洗出 JSON")
    void parsesJsonWrappedInCodeFence() {
        String raw = "```json\n" + VALID_JSON + "\n```";

        AiPlanResult result = AiPlanParser.parse(raw);

        assertNotNull(result, "代码块包裹不应导致解析失败");
        assertEquals("减脂周计划", result.getTitle());
    }

    @Test
    @DisplayName("JSON 前后夹带解释性文字时能提取出主体")
    void parsesJsonWithSurroundingText() {
        String raw = "好的，这是我为你制定的计划：\n" + VALID_JSON + "\n希望你喜欢！";

        AiPlanResult result = AiPlanParser.parse(raw);

        assertNotNull(result);
        assertEquals("减脂周计划", result.getTitle());
    }

    @Test
    @DisplayName("days 为空 → 视为解析失败（交给上层重试）")
    void emptyDaysTreatedAsFailure() {
        String raw = "{\"title\":\"空计划\",\"days\":[]}";

        assertNull(AiPlanParser.parse(raw));
    }

    @Test
    @DisplayName("动作名为空的条目应被丢弃，整个训练日无动作时该日被移除")
    void dropsExercisesWithoutName() {
        String raw = "{\"title\":\"t\",\"days\":[{\"day\":1,\"bodyPart\":\"CHEST\","
                + "\"exercises\":[{\"name\":\"\",\"sets\":3},{\"sets\":3}]}]}";

        // 所有动作都没有名字 → 该日被丢弃 → 计划为空 → 返回 null
        assertNull(AiPlanParser.parse(raw));
    }

    @Test
    @DisplayName("完全不是 JSON 时返回 null，不抛异常")
    void returnsNullForGarbage() {
        assertNull(AiPlanParser.parse("抱歉，我无法完成这个请求。"));
        assertNull(AiPlanParser.parse(""));
        assertNull(AiPlanParser.parse(null));
    }

    @Test
    @DisplayName("缺少 day 字段时按顺序自动编号")
    void autoNumbersMissingDay() {
        String raw = "{\"title\":\"t\",\"days\":[{\"bodyPart\":\"CHEST\","
                + "\"exercises\":[{\"name\":\"卧推\"}]}]}";

        AiPlanResult result = AiPlanParser.parse(raw);

        assertNotNull(result);
        assertEquals(1, result.getDays().get(0).getDay());
    }

    @Test
    @DisplayName("cleanJson 只取首个 { 到最后一个 }")
    void cleansToJsonBody() {
        String cleaned = AiPlanParser.cleanJson("prefix {\"a\":1} suffix");

        assertEquals("{\"a\":1}", cleaned);
    }

    @Test
    @DisplayName("缺少可选字段不应报错（title/notes 为空）")
    void toleratesMissingOptionalFields() {
        String raw = "{\"days\":[{\"day\":1,\"bodyPart\":\"CARDIO\","
                + "\"exercises\":[{\"name\":\"椭圆机\"}]}]}";

        AiPlanResult result = AiPlanParser.parse(raw);

        assertNotNull(result);
        assertNull(result.getTitle());
        assertTrue(result.getDays().get(0).getExercises().size() == 1);
    }

    // ------------------------------------------------------------------
    // bodyPart 归一化
    // ------------------------------------------------------------------

    @Test
    @DisplayName("中文部位应归一化为枚举（胸→CHEST、腿→LEG、有氧→CARDIO）")
    void normalizesChineseBodyPart() {
        assertEquals("CHEST", AiPlanParser.normalizeBodyPart("胸"));
        assertEquals("LEG", AiPlanParser.normalizeBodyPart("腿部"));
        assertEquals("CARDIO", AiPlanParser.normalizeBodyPart("有氧"));
        assertEquals("BACK", AiPlanParser.normalizeBodyPart("背"));
        assertEquals("SHOULDER", AiPlanParser.normalizeBodyPart("肩"));
        assertEquals("CORE", AiPlanParser.normalizeBodyPart("核心"));
    }

    @Test
    @DisplayName("英文枚举（任意大小写）应保持可用")
    void keepsEnglishBodyPart() {
        assertEquals("CHEST", AiPlanParser.normalizeBodyPart("chest"));
        assertEquals("BACK", AiPlanParser.normalizeBodyPart("BACK"));
    }

    @Test
    @DisplayName("中文部位解析后能直接用于下游 switch 匹配")
    void chineseBodyPartBecomesUsableEnum() {
        String raw = "{\"title\":\"t\",\"days\":[{\"day\":1,\"bodyPart\":\"胸\","
                + "\"exercises\":[{\"name\":\"卧推\"}]}]}";

        AiPlanResult result = AiPlanParser.parse(raw);

        assertNotNull(result);
        // 归一化前这里会是 "胸"，导致课程推荐 switch 匹配不到
        assertEquals("CHEST", result.getDays().get(0).getBodyPart());
    }

    // ------------------------------------------------------------------
    // 规模上限
    // ------------------------------------------------------------------

    @Test
    @DisplayName("训练日超过 14 天应被截断")
    void truncatesTooManyDays() {
        StringBuilder sb = new StringBuilder("{\"title\":\"t\",\"days\":[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"day\":").append(i + 1)
              .append(",\"bodyPart\":\"CHEST\",\"exercises\":[{\"name\":\"卧推\"}]}");
        }
        sb.append("]}");

        AiPlanResult result = AiPlanParser.parse(sb.toString());

        assertNotNull(result);
        assertEquals(14, result.getDays().size(), "天数应被截断到 14");
    }

    @Test
    @DisplayName("单日动作超过 15 个应被截断")
    void truncatesTooManyExercises() {
        StringBuilder sb = new StringBuilder("{\"title\":\"t\",\"days\":[{\"day\":1,"
                + "\"bodyPart\":\"CHEST\",\"exercises\":[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"name\":\"动作").append(i).append("\"}");
        }
        sb.append("]}]}");

        AiPlanResult result = AiPlanParser.parse(sb.toString());

        assertNotNull(result);
        assertEquals(15, result.getDays().get(0).getExercises().size(), "单日动作应被截断到 15");
    }
}
