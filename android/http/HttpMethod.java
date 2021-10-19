
package com.archly.mhh.oversea.core.framework.net.http;

public enum HttpMethod {
    POST("POST"),
    GET("GET"),
    ;

    String name;

    HttpMethod(String name) {
        this.name = name;
    }
}
