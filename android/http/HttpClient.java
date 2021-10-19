package com.archly.mhh.oversea.core.framework.net.http;

import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    int retryCount;
    int connectTimeout;
    int readTimeout;
    final Map<String, String> headers = new HashMap<>();

    NetExecutor executor;

    private HttpClient() {
        executor = new NetExecutor();
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {

        private Map<String, String> headers = new HashMap<>();
        private int retryCount = 1;
        private int connectTimeout = 10 * 1000;
        private int readTimeout = 15 * 1000;

        private Builder() {
        }


        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }


        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public HttpClient build() {
            HttpClient client = new HttpClient();
            client.connectTimeout = this.connectTimeout;
            client.readTimeout = this.readTimeout;
            client.headers.putAll(this.headers);
            client.retryCount = this.retryCount;
            return client;
        }
    }


    public HttpCall newCall(Request request) {
        return HttpCall.newRealCall(this, request);
    }


}
