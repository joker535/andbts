package cn.guye.tools.jrpclib;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cn.guye.tools.jrpclib.JRpc.RpcCallBack;

public class RpcCall extends RpcObject{
	@Expose
    @SerializedName("params")
    private JsonArray params;
    
	@Expose(serialize = false, deserialize = false)
    private RpcCallBack callback; 
    
    public RpcCall(long id , String method, JsonArray params) {
        super(id);
        this.method = method;
        this.params = params;
    }

	public RpcCallBack getCallback() {
		return callback;
	}

	public void setCallback(RpcCallBack callback) {
		this.callback = callback;
	}

    public JsonArray getParams() {
        return params;
    }
}
