package com.archly.mhh.oversea.core.framework.net;

import com.archly.mhh.oversea.core.framework.net.http.Response;
import com.archly.mhh.oversea.core.utils.GsonUtils;

public abstract class NetResponseAdapter extends NetCallback<NetResponse> {
    @Override
    public NetResponse onParseResponse(Response.ResponseBody body) {
        return GsonUtils.getGson().fromJson(body.asString(), NetResponse.class);
    }


}
