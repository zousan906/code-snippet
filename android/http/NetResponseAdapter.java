package com.code.snippet;


import org.json.JSONObject;

public abstract class NetResponseAdapter extends NetCallback<JSONObject> {
    @Override
    public JSONObject onParseResponse(Response.ResponseBody body) {
        return GsonUtils.getGson().fromJson(body.asString(), JSONObject.class);
    }


}
