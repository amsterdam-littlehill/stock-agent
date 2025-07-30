package com.jd.genie.agent.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
public class OkHttpUtil {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 创建带有超时设置的 OkHttpClient
     *
     * @param connectTimeout 连接超时时间
     * @param readTimeout    读取超时时间
     * @param writeTimeout   写入超时时间
     * @return 配置好超时的 OkHttpClient 实例
     */
    private static OkHttpClient createClient(long connectTimeout, long readTimeout, long writeTimeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 发送 POST 请求，以 JSON 格式传递参数
     *
     * @param url        请求的 URL
     * @param jsonParams JSON 格式的参数
     * @return 请求结果
     * @throws IOException 网络请求异常
     */
    public static String postJson(String url, String jsonParams, Map<String, String> headers, Long timeout) throws IOException {
        OkHttpClient client = createClient(timeout, timeout, timeout);
        RequestBody body = RequestBody.create(jsonParams, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }
        return null;
    }

    /**
     * 发送 SSE 流式请求
     *
     * @param url           请求的 URL
     * @param headers       请求头参数
     * @param eventListener 事件监听器，用于处理接收到的 SSE 事件
     */
    public static void sseRequest(String url, String jsonParams, Map<String, String> headers, Long timeout, SseEventListener eventListener) {
        OkHttpClient client = createClient(timeout, timeout, timeout);
        RequestBody body = RequestBody.create(jsonParams, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                eventListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            eventListener.onEvent(line);
                        }
                    }
                    eventListener.onComplete();
                } else {
                    eventListener.onError(new IOException("SSE request failed with status code: " + response.code()));
                }
            }
        });
    }

    /**
     * SSE 事件监听器接口
     */
    public interface SseEventListener {
        /**
         * 处理接收到的 SSE 事件
         *
         * @param event 接收到的事件内容
         */
        void onEvent(String event);

        /**
         * 处理请求完成事件
         */
        void onComplete();

        /**
         * 处理请求错误事件
         *
         * @param e 异常对象
         */
        void onError(IOException e);
    }

}