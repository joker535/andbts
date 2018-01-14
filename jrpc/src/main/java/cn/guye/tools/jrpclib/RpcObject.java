package cn.guye.tools.jrpclib;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class RpcObject {
	@Expose
	@SerializedName("id")
	private long id;

	@Expose
	@SerializedName("method")
	protected String method;

	@Expose(serialize = false, deserialize = false)
	private long dependent = -1;

	@Expose
	@SerializedName("jsonrpc")
	private final String version = "2.0";

	public RpcObject(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getMethod() {
		return method;
	}

	public String toJson(Gson gson) {
		return gson.toJson(this);
	}

	public long getDependent() {
		return dependent;
	}

	public void setDependent(long dependent) {
		this.dependent = dependent;
	}

}
