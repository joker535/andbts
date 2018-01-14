package cn.guye.tools.jrpclib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.guye.tools.jrpclib.JRpc.RpcCallBack;
import cn.guye.tools.jrpclib.JRpc.RpcHandle;

public class Main {
	static  JRpc jRpc;
	public static void main(String[] args) {
		 jRpc = JRpcHelper.getJRpc("wss://bitshares-api.wancloud.io/ws", new RpcHandle() {
			
			@Override
			public void onDisconnect(Throwable throwable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnect() {
				jRpc.call("lookup_asset_symbols", new String[][]{new String[]{"CNY"}}, new RpcCallBack() {

					@Override
					public void onResult(RpcReturn result) {
						Gson gson = new GsonBuilder()
				        .excludeFieldsWithoutExposeAnnotation()
				        .create();
						System.out.println(gson.toJson(result));
						jRpc.close();
						
					}

					@Override
					public void onException(Throwable throwable) {
						// TODO Auto-generated method stub
						
					}}
				);
				
			}
		});
		 jRpc.startConnect();
	}
}
