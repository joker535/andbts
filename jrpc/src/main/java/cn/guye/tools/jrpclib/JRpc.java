package cn.guye.tools.jrpclib;

import com.google.gson.JsonArray;

import java.util.Map;

public interface JRpc {
	public interface RpcCallBack {
		public void onResult(RpcReturn result);

		public void onException(Throwable throwable);
	}

	public interface RpcNoticeHandle {
		public void onNotice(RpcNotice notice);
	}

	public interface RpcHandle {
		public void onDisconnect(Throwable throwable);

		public void onConnect();
	}

	public void startConnect();

	public long call(String method, JsonArray params, RpcCallBack callback);

	public void setNoticeHandle(RpcNoticeHandle noticeHandle);

	public void setErrorHandle(RpcHandle eh);

	public long getRTT();

	public void close();
}
