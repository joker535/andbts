package cn.guye.tools.jrpclib;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WsConncetion {
    private WebSocket ws;
    
    private WebSocketListener webSocketListener;
    
    public WsConncetion(){
    }
    
    private void createWs(String url,WebSocketListener listener){
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30,  TimeUnit.SECONDS).build();  
        Request request = new Request.Builder().url(url).build();  
        webSocketListener = listener;
        ws = client.newWebSocket(request, webSocketListener);
        client.dispatcher().executorService().shutdown();
        
    }

	public void connect(String url,WebSocketListener listener) {
		createWs(url, listener);
	}

	public void send(String text) {
		ws.send(text);
	}

	public void close() {
		ws.close(1000, null);
		
	}
}
