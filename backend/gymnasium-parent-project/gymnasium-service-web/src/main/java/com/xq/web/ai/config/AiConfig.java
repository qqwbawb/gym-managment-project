package com.xq.web.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块配置。
 *
 * 关键点：
 *   项目同时引入了 ollama 与 openai 两个 starter，容器里会有多个 ChatModel 实现。
 *   Spring AI 通过 spring.ai.model.chat 指定「由哪个实现装配为默认 ChatModel」，
 *   autoconfigure 后只会暴露一个 ChatModel Bean，这里直接注入即可。
 *
 *   想切换模型时不用改代码，改配置即可：
 *     AI_PROVIDER=ollama   （本地免费，需先安装 Ollama 并拉取模型）
 *     AI_PROVIDER=openai   （在线，配合 OPENAI_BASE_URL / OPENAI_API_KEY / OPENAI_MODEL）
 */
@Slf4j
@Configuration
public class AiConfig {

    /**
     * 未配置真实 API Key 时使用的哨兵值。
     *
     * 为什么需要它：Spring AI 的 openAiApi Bean 创建时强制校验 key 非空，
     * 空字符串会导致整个应用启动失败。给哨兵值既能让 Bean 正常创建，
     * 又能让我们在真实调用前识别出「没配 key」并返回友好提示。
     */
    public static final String PLACEHOLDER_API_KEY = "NOT_CONFIGURED";

    @Value("${spring.ai.model.chat:openai}")
    private String provider;

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    /**
     * 构建 ChatClient。
     *
     * 说明：系统提示词不在这里固定成 defaultSystem，而是每次调用时显式传入，
     * 这样 Prompt 内容对调用方可见，便于测试与调整。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        if (isOpenAiProvider() && !isApiKeyConfigured()) {
            log.warn("AI 健身教练：当前使用 openai 协议但未配置 API Key，"
                    + "应用可正常启动，但 AI 生成接口会返回提示信息。"
                    + "请设置环境变量 DEEPSEEK_API_KEY 后重启。");
        }
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 当前是否使用 openai 协议（DeepSeek / 豆包等在线服务）。
     */
    public boolean isOpenAiProvider() {
        return "openai".equalsIgnoreCase(provider);
    }

    /**
     * 是否已配置可用的 API Key。
     */
    public boolean isApiKeyConfigured() {
        return openAiApiKey != null
                && !openAiApiKey.trim().isEmpty()
                && !PLACEHOLDER_API_KEY.equals(openAiApiKey.trim());
    }

    /**
     * 启动前自检：返回一句人类可读的状态描述，供接口返回给前端。
     */
    public String describeStatus() {
        if (isOpenAiProvider()) {
            return isApiKeyConfigured()
                    ? "在线模型已就绪"
                    : "未配置 API Key，AI 功能暂不可用";
        }
        return "本地模型模式";
    }

    public String currentProvider() {
        return provider;
    }
}
