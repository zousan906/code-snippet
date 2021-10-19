package com.code.snippet;

public interface Callback {


    void onFailure(HttpCall call, Exception e);

    void onResponse(HttpCall call, Response response);
}
