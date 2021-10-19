package com.archly.mhh.oversea.core.framework.net.http;

public interface Callback {


    void onFailure(HttpCall call, Exception e);

    void onResponse(HttpCall call, Response response);
}
