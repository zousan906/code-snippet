package com.code.snippet;

import android.util.Log;


import java.io.Closeable;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class Request implements Closeable {
    private static final String TAG = "NET.request";

    int retryCount;
    int connectTimeout;
    int readTimeout;
    Map<String, String> headers;

    String url;
    RequestBody body;
    HttpMethod method;
    HttpURLConnection conn;

    volatile boolean closed = false;

    public Request(Builder builder) {
        this.retryCount = builder.retryCount;
        this.url = builder.httpUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.headers = builder.headers;
        this.method = builder.method;
        if (this.method == HttpMethod.GET && !builder.params.isEmpty()) {
            String url = Utils.getUrl(this.url, builder.params);
            this.url = url;
        }
        this.body = builder.body;
    }


    public void close() {
        if (closed) return;
        closed = true;
        if (conn != null) {
            conn.disconnect();
        }
    }

    public static class Builder {

        private String httpUrl;
        private Map<String, String> params;
        private Map<String, String> headers = new HashMap<>();
        private int retryCount = 1;
        private int connectTimeout = 0;
        private int readTimeout = 0;
        private RequestBody body;
        private HttpMethod method;

        public Builder(String url) {
            this.httpUrl = url;
        }

        public Builder post(RequestBody body) {
            this.body = body;
            this.method = HttpMethod.POST;
            return this;
        }


        public Builder get() {
            this.method = HttpMethod.GET;
            return this;
        }

        public Builder get(Map<String, String> params) {
            this.method = HttpMethod.GET;
            this.params = params;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
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

        public Request build() {
            return new Request(this);
        }
    }

}
