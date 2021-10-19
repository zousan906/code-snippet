package com.archly.mhh.oversea.core.framework.net.http;

public class NetException extends RuntimeException{

    public NetException(String message) {
        super(message);
    }

    public NetException(String message, Throwable cause) {
        super(message, cause);
    }
}
