package cn.guye.tools.jrpclib;

import cn.guye.tools.jrpclib.JRpc.RpcHandle;

public class JRpcHelper {
	public static JRpc getJRpc(String url,RpcHandle handle){
		
		return new JRpcImpl(url , handle);
	}
}
