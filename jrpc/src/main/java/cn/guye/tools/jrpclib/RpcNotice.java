package cn.guye.tools.jrpclib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RpcNotice  extends RpcObject{
	@Expose
    @SerializedName("params")
    private Object params;
    
    public RpcNotice(long id) {
        super(-1);
    }

}
