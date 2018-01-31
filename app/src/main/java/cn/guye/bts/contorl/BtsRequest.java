package cn.guye.bts.contorl;

import com.google.gson.JsonElement;

import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/1/22.
 */

public class BtsRequest {

    private long id;

    private String api;
    private String method;
    private Object[] params;

    private CallBack callBack;

    private Object tag;

    public BtsRequest(String api, String method, Object[] params,CallBack callBack) {
        this.api = api;
        this.method = method;
        this.params = params;
        this.callBack = callBack;
    }

    public interface CallBack{
        void onResult(BtsRequest request, JsonElement data);
        void onError(JRpcError error);
    }

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public CallBack getCallBack() {
        return callBack;

    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {

        return tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
