package com.archly.mhh.oversea.core.framework.net.http;

import android.text.TextUtils;

import com.archly.mhh.oversea.core.utils.LogUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class HttpCall {


    private static final String TAG = "MHH.http";
    protected HttpClient client;
    protected Request originalRequest;


    HttpCall(HttpClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }

    static HttpCall newRealCall(HttpClient client, Request originalRequest) {
        return new RealCall(client, originalRequest);
    }


    public void executeAsync(Callback callback) {
        client.executor.execute(new AsyncCall(this, callback));
    }


    public abstract Response execute();


    /**
     * 得到 HttpURLConnection 对象，并进行一些设置
     *
     * @param request 请求对象
     * @throws IOException IOException
     */
    protected HttpURLConnection openConnection(Request request) throws IOException {
        URL url = new URL(request.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(request.method.name);
        //不使用缓存
        conn.setUseCaches(false);
        //设置超时时间
        conn.setConnectTimeout(request.connectTimeout > 0 ? request.connectTimeout : client.connectTimeout);
        //设置读取超时时间
        conn.setReadTimeout(request.readTimeout > 0 ? client.readTimeout : client.readTimeout);
        // 设置client 初始化的header
        setHeader(conn, client.headers);
        // 设置 请求header,可能会覆盖 client 初始化的header
        setHeader(conn, request.headers);
        request.conn = conn;
        return conn;
    }

    /**
     * 设置请求头
     *
     * @param conn      HttpURLConnection
     * @param headerMap 请求头键值对
     */
    private void setHeader(HttpURLConnection conn, Map<String, String> headerMap) {
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                conn.setRequestProperty(key, headerMap.get(key));
            }
        }
    }


    abstract Response doRequest();


    static class RealCall extends HttpCall {

        private AtomicBoolean executed = new AtomicBoolean(false);

        RealCall(HttpClient client, Request originalRequest) {
            super(client, originalRequest);
        }

        public Response execute() {
            if (executed.compareAndSet(false, true)) {
                try {
                    client.executor.executed(this);
                    return doRequest();
                } finally {
                    client.executor.finished(this);
                }
            }
            throw new NetException("Already Executed");
        }


        @Override
        Response doRequest() {
            try {
                HttpURLConnection conn = openConnection(originalRequest);
                RequestBody body = originalRequest.body;
                if (body != null) {
                    if (!TextUtils.isEmpty(body.contentType())) {
                        conn.setRequestProperty("Content-Type", body.contentType());
                    }

                    if (body.hasContent()) {
                        conn.setDoOutput(true);
                    }
                }

                conn.connect();

                if (conn.getDoOutput()) {
                    body.writeTo(conn.getOutputStream());
                }

                return Response.build(originalRequest);
            } catch (Exception e) {
                //异常关闭 资源
                Utils.closeQuietly(originalRequest);
                return Response.buildExceptionResponse(e);
            }
        }
    }


    static class AsyncCall implements Runnable {
        private HttpCall originalCall;
        private Callback callback;

        public AsyncCall(HttpCall originalCall, Callback callback) {
            this.originalCall = originalCall;
            this.callback = callback;
        }

        @Override
        public void run() {
            boolean isCallback = false;
            try {
                Response response = originalCall.doRequest();
                isCallback = true;
                callback.onResponse(originalCall, response);
            } catch (Exception e) {
                LogUtils.printStackTrace(TAG, "Request task ex", e);
                if (!isCallback) {
                    callback.onFailure(originalCall, e);
                }
            } finally {
                originalCall.client.executor.finished(this);
            }
        }


        void executeWith(ExecutorService executor) {
            boolean success = false;
            try {
                executor.execute(this);
                success = true;
            } catch (Exception e) {
                callback.onFailure(originalCall, e);
            } finally {
                if (!success) {
                    originalCall.client.executor.finished(this);  // 未提交成功
                }
            }
        }
    }
}
