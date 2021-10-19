package com.archly.mhh.oversea.core.framework.net;

import android.util.Log;

import com.archly.mhh.oversea.core.config.SettingConfig;
import com.archly.mhh.oversea.core.encryption.DataAes;
import com.archly.mhh.oversea.core.encryption.MD5;
import com.archly.mhh.oversea.core.framework.callback.CallBackListener;
import com.archly.mhh.oversea.core.framework.net.http.Callback;
import com.archly.mhh.oversea.core.framework.net.http.HttpCall;
import com.archly.mhh.oversea.core.framework.net.http.HttpClient;
import com.archly.mhh.oversea.core.framework.net.http.NamedThreadFactory;
import com.archly.mhh.oversea.core.framework.net.http.NetExecutor;
import com.archly.mhh.oversea.core.framework.net.http.Request;
import com.archly.mhh.oversea.core.framework.net.http.RequestBody;
import com.archly.mhh.oversea.core.framework.net.http.Response;
import com.archly.mhh.oversea.core.utils.GsonUtils;
import com.archly.mhh.oversea.core.utils.LogUtils;
import com.archly.mhh.oversea.core.utils.SortUtils;
import com.archly.mhh.oversea.core.utils.TransUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetConnector {
    private static final String TAG = "MHH.Net";
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


    public static void post(String url, Object param, CallBackListener<NetResponse> listener) {
        try {
            String data = GsonUtils.getGson().toJson(encode(param));
            instance().post(url, data, new NetResponseAdapter() {


                @Override
                public void onFailure(int code, String errorMessage, Exception e) {
                    listener.onFailed(code, errorMessage, null);
                }

                @Override
                public void onResponse(NetResponse response) {
                    listener.onSuccess(response);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static NetRequest encode(Object obj) throws Exception {
        String json = GsonUtils.getGson().toJson(obj);
        LogUtils.i(json);
        DataAes aes = new DataAes();
        String data = aes.encrypt(json);
        Map<String, Object> map = TransUtils.obj2map(obj);
        map = SortUtils.sort(map);
        String unMd5 = TransUtils.map2string(map, "=", "&");
        LogUtils.i(unMd5);
        String sign = MD5.md5(unMd5 + SettingConfig.getSignKey());
        String version = "v1";
        return new NetRequest(data, sign, version);
    }

}
