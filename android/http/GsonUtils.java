package com.code.snippet;

import com.archly.mhh.oversea.core.framework.data.adapter.GsonTypeAdapter;
import com.archly.mhh.oversea.core.libs.google.gson.Gson;
import com.archly.mhh.oversea.core.libs.google.gson.GsonBuilder;
import com.archly.mhh.oversea.core.libs.google.gson.reflect.TypeToken;

import java.util.Map;

public class GsonUtils {

    private static Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }

    public static Gson createCustomGson() {
        return new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, Object>>() {
        }.getType(), new GsonTypeAdapter()).create();
    }

}
