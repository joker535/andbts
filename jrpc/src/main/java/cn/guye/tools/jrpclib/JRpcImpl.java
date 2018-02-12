package cn.guye.tools.jrpclib;

import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class JRpcImpl implements JRpc {

	private WsConncetion wsConncetion;
	private WebSocketListener wsListener;
	private AtomicLong id = new AtomicLong(1);
	private String url;
	private RpcHandle handle;
	private Map<Long, RpcCall> paddingCall = new LinkedHashMap<>(1000);
	private Gson gson;
	private RpcNoticeHandle rpcNoticeHandle;

	public JRpcImpl(String url, RpcHandle handle) {
		wsConncetion = new WsConncetion();
		wsListener = new WsListener();
		this.url = url;
		this.handle = handle;
		gson = new GsonBuilder().serializeNulls()
		        .excludeFieldsWithoutExposeAnnotation()
		        .create();
	}

	public void startConnect() {
		wsConncetion.connect(url, wsListener);
	}

	@Override
	public long call(String method, JsonArray params, RpcCallBack callback) {
		RpcCall call = new RpcCall(id.getAndIncrement(), method, params);
		call.setCallback(callback);
		synchronized (this) {
			paddingCall.put(call.getId(), call);
		}
		wsConncetion.send(gson.toJson(call));
		System.out.println(gson.toJson(call));
		return call.getId();
	}

	@Override
	public void setNoticeHandle(RpcNoticeHandle noticeHandle) {
		rpcNoticeHandle = noticeHandle;
	}

	@Override
	public void setErrorHandle(RpcHandle eh) {
		handle = eh;

	}

	@Override
	public long getRTT() {
		// TODO Auto-generated method stub
		return 0;
	}

	public class WsListener extends WebSocketListener {
		@Override
		public void onClosed(WebSocket webSocket, int code, String reason) {
			System.out.println("closed : " + reason);
			handle.onDisconnect(new SocketException(code + reason));
		}

		@Override
		public void onClosing(WebSocket webSocket, int code, String reason) {
			System.out.println("closing :" + reason);
		}

		@Override
		public void onFailure(WebSocket webSocket, Throwable t, Response response) {
			t.printStackTrace();
			handle.onDisconnect(t);
		}

		@Override
		public void onMessage(WebSocket webSocket, String text) {
			if (text == null || text.length() == 0) {
				return;
			}
			System.out.println(text);
			RpcObject rpcObject;
			JsonParser parse = new JsonParser();
			JsonElement jsonElement = parse.parse(text);
			JsonElement id = jsonElement.getAsJsonObject().get("id");
			if (id == null) {
				rpcObject = gson.fromJson(jsonElement, RpcNotice.class);
				rpcNoticeHandle.onNotice((RpcNotice) rpcObject);
			} else {
				rpcObject = gson.fromJson(jsonElement, RpcReturn.class);
				RpcCall call = getCall(rpcObject.getId());
				((RpcReturn) rpcObject).setCall(call);
				call.getCallback().onResult((RpcReturn) rpcObject);
			}
			// TODO

		}

		private synchronized RpcCall getCall(long id) {
			return paddingCall.get(id);
		}

		@Override
		public void onOpen(WebSocket webSocket, Response response) {
			System.out.println("open");
			handle.onConnect();
		}

	}

	@Override
	public void close() {
		wsConncetion.close();
		
	}

}
