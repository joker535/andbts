package cn.guye.tools.jrpclib;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RpcNotice  extends RpcObject{
	@Expose
    @SerializedName("params")
    private JsonElement params;
    
    public RpcNotice(long id) {
        super(-1);
    }

    public JsonElement getParams() {
        return params;
    }
}
