package com.code.snippet;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetConnector {
    private static final String TAG = "Net";
    private HttpClient client;

    /**
     * 同步请求线程池
     */
    private volatile static ExecutorService syncExecutor = null;

    private NetConnector() {
        client = HttpClient.builder().build();

        syncExecutor = new ThreadPoolExecutor(0, 16,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new NamedThreadFactory("sync"));
    }

    public static final class InstanceHolder {
        private static final NetConnector INSTANCE = new NetConnector();
    }

    private static NetConnector instance() {
        return InstanceHolder.INSTANCE;
    }


    private Callback callbackDecorate(NetCallback callback) {

        return new Callback() {
            @Override
            public void onFailure(HttpCall call, Exception e) {
                callback.onFailure(-1, e.getMessage(), e);
            }

            @Override
            public void onResponse(HttpCall call, Response response) {

                if (response.isSuccessful()) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(response);
                }
            }
        };
    }

    private <T> Callable<T> syncDecorate(HttpCall call, Class<T> parseType) {
        return () -> {
            try (Response response = call.execute()) {

                if (response.isSuccessful()) {
                    String s = response.responseBody.asString();
                    T t = GsonUtils.getGson().fromJson(s, parseType);
                    return t;
                }

                Log.e(TAG, response.errorMsg, response.exception);
                return null;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return null;
            }
        };
    }

    private <T> T syncRequest(Callable<T> syncWrapper) {
        try {
            return syncExecutor.submit(syncWrapper).get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    private void post(String url, String jsonContent, NetCallback callback) {
        Request request = new Request.Builder(url)
                .post(new RequestBody.JsonBody(jsonContent))
                .build();

        HttpCall httpCall = client.newCall(request);
        httpCall.executeAsync(callbackDecorate(callback));
    }




}
