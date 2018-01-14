package cn.guye.bitshares;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.guye.tools.jrpclib.JRpc;
import cn.guye.tools.jrpclib.JRpcHelper;
import cn.guye.tools.jrpclib.RpcCall;
import cn.guye.tools.jrpclib.RpcReturn;

public class BtsApi {

    public static final int status_connected = 10;
    public static final int status_connection = 11;
    public static final int status_closing = 12;
    public static final int status_closed = 13;
    public static final int status_broken = 14;

    private JRpc jRpc;
    private int status;
    private List<BtsRpcListener> btsHandles = new LinkedList<>();
    private List<DataListener> dataLinstener = new LinkedList<>();
    private Map<String , Integer> apiIds = new HashMap<>(4);

    private BtsHandle btsHandle;
    private BtsCallBack btsCallBack;

    public BtsApi(String url){
        btsHandle = new BtsHandle();
        btsCallBack = new BtsCallBack();
        jRpc = JRpcHelper.getJRpc(url,btsHandle);
    }

    public void start(){
        jRpc.startConnect();
    }

    public interface DataListener{
        public void onResult(JsonElement result);
        public void onError();
        public void onDataChange();
    }

    public interface BtsRpcListener{
        public void onOpen();
        public void onFialed();
        public void onClosed();
        public void onError();
    }

    public int getApiId(String api){
        return 2;
    }

    public int addDataListener(DataListener dataListener){
        return 0;
    }

    public long call(int apiId , String method , Object[] param){
        Object[] p = new Object[param.length +2];
        p[0] = apiId;
        p[1] = method;
        System.arraycopy(param,0,p,2,param.length);
        return jRpc.call(RPC.CALL,p,btsCallBack);
    }

    public long call(int apiId , String method , List param){
        param.add(0,apiId);
        param.add(1,method);
        Object[] p = new Object[param.size()];
        param.toArray(p);
        return jRpc.call(RPC.CALL,p,btsCallBack);
    }

    private class BtsHandle implements JRpc.RpcHandle {

        @Override
        public void onDisconnect(Throwable throwable) {
            for (BtsRpcListener b:
                    btsHandles) {
                b.onClosed();
            }
        }

        @Override
        public void onConnect() {
            RPC.login(BtsApi.this,"","");
        }
    }

    private class BtsCallBack implements JRpc.RpcCallBack{

        @Override
        public void onResult(RpcReturn result) {
            if(result.getError() != null){

            }else{
                if(result.getCall().getParams()[2].equals(RPC.CALL_LOGIN)){
                    RPC.database(BtsApi.this);
                }else if(result.getCall().getParams()[2].equals(RPC.CALL_DATABASE)){
                    apiIds.put(RPC.CALL_DATABASE , result.getResult().getAsInt());
                    RPC.history(BtsApi.this);
                }else if(result.getCall().getParams()[2].equals(RPC.CALL_HISTORY)){
                    apiIds.put(RPC.CALL_HISTORY , result.getResult().getAsInt());
                    RPC.network_broadcast(BtsApi.this);
                }else if(result.getCall().getParams()[2].equals(RPC.CALL_NETWORK_BROADCAST)){
                    apiIds.put(RPC.CALL_NETWORK_BROADCAST , result.getResult().getAsInt());
                    for (BtsRpcListener b:
                         btsHandles) {
                        b.onOpen();
                    }
                }else{
                    for (DataListener dl:dataLinstener) {
                        dl.onResult(result.getResult());
                    }
                }
            }
        }

        @Override
        public void onException(Throwable throwable) {

        }
    }

}
