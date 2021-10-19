
package com.code.snippet;

public enum HttpMethod {
    POST("POST"),
    GET("GET"),
    ;

    String name;

    HttpMethod(String name) {
        this.name = name;
    }
}
