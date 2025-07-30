package com.jd.genie.agent.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * LLM 配置类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMSettings {
    /**
     * 模型名称
     */
    private String model;

    /**
     * 最大生成 token 数量
     */
    private int maxTokens;

    /**
     * 温度参数
     */
    private double temperature;

    /**
     * API 类型（openai 或 azure）
     */
    private String apiType;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * API 版本（仅适用于 Azure）
     */
    private String apiVersion;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 接口 URL
     */
    private String interfaceUrl;

    /**
     * FunctionCall类型
     */
    private String functionCallType;

    /**
     * 最大输入 token 数量
     */
    private int maxInputTokens;

    /**
     * 额外参数
     */
    private Map<String, Object> extParams;

}