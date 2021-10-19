package com.archly.mhh.oversea.core.framework.net;

import android.text.TextUtils;

import com.archly.mhh.oversea.core.framework.net.http.Response;

public abstract class NetCallback<T> {


    final void onError(final Response response) {
        final String errorMessage;
        if (!TextUtils.isEmpty(response.errorMsg)) {
            errorMessage = response.errorMsg;
        } else if (response.exception != null) {
            errorMessage = response.exception.toString();
        } else {
            errorMessage = "unknown error";
        }
        onFailure(response.code, errorMessage, response.exception);
    }


    final void onSuccess(Response response) {
        final T obj = onParseResponse(response.responseBody);
        onResponse(obj);
    }

    /**
     * 解析 ResponseBody
     *
     * @param responseBody 网络请求返回信息
     * @return T
     */
    public abstract T onParseResponse(Response.ResponseBody responseBody);

    /**
     * 访问网络失败后被调用
     *
     * @param code         请求返回的错误 code
     * @param errorMessage 错误信息
     */
    public abstract void onFailure(int code, String errorMessage, Exception e);

    /**
     * 访问网络成功后被调用
     *
     * @param response 处理后的对象
     */
    public abstract void onResponse(T response);


}
