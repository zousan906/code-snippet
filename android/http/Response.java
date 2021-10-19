package com.code.snippet;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Locale;

/**
 * Response 在使用后需要 关闭资源  {@link #close()}
 */
public class Response implements Closeable {
    public String errorMsg;
    public int code = -1;
    public long contentLength;
    public Exception exception;
    public ResponseBody responseBody;
    private Request request;

    public Response(Request request) {
        this.request = request;
    }


    public static Response build(Request request) {
        Response response = new Response(request);
        try {
            response.parse();
        } catch (IOException e) {
            return buildExceptionResponse(e);
        }

        return response;
    }

    private void parse() throws IOException {
        HttpURLConnection conn = request.conn;
        this.code = conn.getResponseCode();
        this.contentLength = conn.getContentLength();
        // 当 ResponseCode 小于 HTTP_BAD_REQUEST（400）时，获取返回信息
        if (this.code < HttpURLConnection.HTTP_BAD_REQUEST) {
            this.responseBody = new ResponseBody(conn.getInputStream());
        } else {
            try {
                this.errorMsg = Utils.streamAsString(conn.getErrorStream());
            } catch (Exception exception) {
                //none.
            } finally {
                close();
            }
        }
    }


    public boolean isSuccessful() {
        return code > 0 && code < HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * 发生异常时，返回包含异常信息的 RealResponse 对象
     *
     * @return RealResponse 异常信息
     */
    static Response buildExceptionResponse(Exception e) {
        Response response = new Response(null);
        response.exception = e;
        response.errorMsg = e.getMessage();
        return response;
    }


    @Override
    public void close() {
        Utils.closeQuietly(responseBody);
        Utils.closeQuietly(request);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "code:%d\nerrorMsg:%s\nexception:%s",
                code, errorMsg,
                exception == null ? "" : exception.getMessage());
    }


    /**
     * responseBody 使用后需要 关闭资源 {@link #close()}
     * responseBody 只能被消费一次
     * 如果 调用 {@link #asString()} 则无需手动调用
     */
    public class ResponseBody implements Closeable {

        private InputStream inputStream;
        private volatile boolean closed = false;

        public ResponseBody(InputStream inputStream) {
            this.inputStream = inputStream;
        }


        public String asString() {
            canConsumed();
            try {
                return Utils.streamAsString(inputStream);
            } finally {
                Response.this.close();
            }
        }

        /**
         * 调用 stream 需要手动关闭 responseBody
         *
         * @return
         */
        public InputStream byteStream() {
            canConsumed();
            return inputStream;
        }


        @Override
        public void close() {
            if (closed) return;
            closed = true;
            Utils.closeQuietly(inputStream);
        }

        private void canConsumed() {
            if (closed) throw new NetException("The response body can be consumed only once ");
        }
    }


}
