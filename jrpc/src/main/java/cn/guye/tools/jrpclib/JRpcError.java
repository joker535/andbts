package cn.guye.tools.jrpclib;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

/**
 * Created by nieyu on 18/1/14.
 */
public class JRpcError {
    @Expose
    private String code;
    @Expose
    private String message;

    @Expose
    private JsonElement data;

    public String getCode() {
        return code;
    }

    public JsonElement getData() {
        return data;
    }

    public String getMessage() {

        return message;
    }
}
