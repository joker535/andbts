package cn.guye.tools.jrpclib;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class RpcReturn extends RpcObject{

    @Expose(serialize = false, deserialize = false)
    private RpcCall call;

    @Expose
    private JsonElement result;
    @Expose
    private JRpcError error;
    
    public RpcReturn(long id , JsonElement result,JRpcError error) {
        super(id);
 
        this.result = result;
        this.error = error;
    }

    public RpcCall getCall() {
        return call;
    }

    public void setCall( RpcCall call ) {
        this.call = call;
    }

    public JsonElement getResult() {
        return result;
    }

    public JRpcError getError() {
        return error;
    }

}
