package com.archly.mhh.oversea.core.framework.net.http;

import static com.archly.mhh.oversea.core.utils.CharSet.CHARSET_UTF8;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public abstract class RequestBody {

    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";
    public static final String APPLICATION_X_FORM_URLENCODED = "application/x-www-form-urlencoded";

    abstract String contentType();


    abstract boolean hasContent();

    abstract void writeTo(OutputStream out);


    public static class FormBody extends RequestBody {
        Map<String, String> body;

        public FormBody(Map<String, String> body) {
            this.body = body;
        }

        @Override
        String contentType() {
            return APPLICATION_X_FORM_URLENCODED;
        }

        @Override
        boolean hasContent() {
            return true;
        }

        @Override
        void writeTo(OutputStream outputStream) {
            String formBody = getFormBody(body);
            Utils.writeTo(outputStream, formBody);
        }


        /**
         * 根据键值对参数得到 body
         *
         * @param params 键值对参数
         * @return 请求 body
         */
        private String getFormBody(Map<String, String> params) {
            if (params != null) {
                StringBuilder result = new StringBuilder();
                boolean first = true;
                try {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (first) {
                            first = false;
                        } else {
                            result.append("&");
                        }
                        result.append(URLEncoder.encode(entry.getKey(), CHARSET_UTF8));
                        result.append("=");
                        result.append(URLEncoder.encode(entry.getValue(), CHARSET_UTF8));
                    }
                    return result.toString();
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            }
            return null;
        }
    }


    public static class JsonBody extends RequestBody {

        private String body;

        public JsonBody(String body) {
            this.body = body;
        }

        @Override
        String contentType() {
            return APPLICATION_JSON_UTF8;
        }

        @Override
        boolean hasContent() {
            return true;
        }

        @Override
        void writeTo(OutputStream out) {
            Utils.writeTo(out, body);
        }
    }
}
