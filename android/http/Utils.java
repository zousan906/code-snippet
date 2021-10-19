
package com.archly.mhh.oversea.core.framework.net.http;

import static com.archly.mhh.oversea.core.utils.CharSet.CHARSET_UTF8;

import android.text.TextUtils;

import com.archly.mhh.oversea.core.utils.GsonUtils;
import com.archly.mhh.oversea.core.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


class Utils {


    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LogUtils.printStackTrace("Close source Error", e);
            }
        }

    }

    /**
     * 不关闭输入流,只关闭内部开启的资源兑现
     *
     * @param is
     * @return
     */
    static String streamAsString(InputStream is) {
        String buf;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, CHARSET_UTF8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            buf = sb.toString();
            return buf;
        } catch (Exception e) {
            LogUtils.printStackTrace("Read Ret Error", e);
        } finally {
            closeQuietly(reader);
        }
        return "";
    }


    /**
     * GET 请求 url 拼接
     *
     * @param path      请求地址
     * @param paramsMap 参数键值对参数
     * @return GET 请求 url 链接
     */
    static String getUrl(String path, Map<String, String> paramsMap) {
        if (path != null && paramsMap != null) {
            if (!path.contains("?")) {
                path = path + "?";
            } else {
                path = path + ("&");
            }
            for (String key : paramsMap.keySet()) {
                path = path + key + "=" + paramsMap.get(key) + "&";
            }
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }


    /**
     * write string content to outputStream
     */
    static void writeTo(OutputStream out, String content) {
        BufferedWriter writer = null;
        try {
            if (!TextUtils.isEmpty(content)) {
                writer = new BufferedWriter(new OutputStreamWriter(out, CHARSET_UTF8));
                writer.write(content);
                writer.flush();
            }
        } catch (Exception e) {
            throw new NetException("write stream error", e.getCause());
        } finally {
            closeQuietly(writer);
        }
    }
}
