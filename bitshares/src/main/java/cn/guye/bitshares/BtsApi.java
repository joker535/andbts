package cn.guye.bitshares;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.guye.bitshares.fc.crypto.ripemd160_object;
import cn.guye.bitshares.models.AccountOptions;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.Authority;
import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bitshares.models.OperationHistory;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.models.chain.config;
import cn.guye.bitshares.wallet.compact_signature;
import cn.guye.bitshares.wallet.types;
import cn.guye.tools.jrpclib.JRpc;
import cn.guye.tools.jrpclib.JRpcHelper;
import cn.guye.tools.jrpclib.RpcNotice;
import cn.guye.tools.jrpclib.RpcReturn;

public class BtsApi {

    public static final int STATUS_CONNECTED = 10;
    public static final int STATUS_CONNECTION = 11;
    public static final int STATUS_CLOSING = 12;
    public static final int STATUS_CLOSED = 13;
    public static final int STATUS_BROKEN = 14;

    private JRpc jRpc;
    private int status;
    private List<BtsRpcListener> btsHandles = new LinkedList<>();
    private List<DataListener> dataLinstener = new LinkedList<>();
    private Map<String , Integer> apiIds = new HashMap<>(4);
    private Map<Long, String> callIds = new HashMap<Long, String>();
    private Gson gson;

    private BtsHandle btsHandle;
    private BtsCallBack btsCallBack;

    public BtsApi(String url){
        btsHandle = new BtsHandle();
        btsCallBack = new BtsCallBack();
        jRpc = JRpcHelper.getJRpc(url,btsHandle);
        jRpc.setNoticeHandle(btsHandle);
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(OperationHistory.class, new OperationHistory.OperationHistoryDeserializer())
                .registerTypeAdapter(Authority.class,new Authority.AuthorityDeserializer())
                .registerTypeAdapter(types.public_key_type.class,new types.public_key_type_deserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .registerTypeAdapter(AccountOptions.class,new AccountOptions.AccountOptionsDeserializer())
                .registerTypeAdapter(LimitOrder.class,new LimitOrder.LimitOrderDeserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountSerializer())
                .registerTypeAdapter(Operations.class,new Operations.OperationDeserializer())
                .registerTypeAdapter(Operations.class,new Operations.OperationSerializer())
                .registerTypeAdapter(ripemd160_object.class, new ripemd160_object.ripemd160_object_deserializer())
                .registerTypeAdapter(compact_signature.class, new compact_signature.compact_signature_serializer())
                .create();
    }

    public void start(){
        jRpc.startConnect();
    }

    public int getStatus() {
        return status;
    }

    public <T> T parse(JsonElement jsonElement,Class<T> tClass){
        return gson.fromJson(jsonElement,tClass);
    }

    public String toJson(Object o) {
        return gson.toJson(o);
    }


    public interface DataListener{
        public void onResult(RpcReturn result);
        public void onError();
        public void onDataChange();
    }

    public interface BtsRpcListener{
        public void onOpen();
        public void onFialed();
        public void onClosed();
        public void onError();
        public void onNotice(RpcNotice rpcNotice);
    }

    public int getApiId(String api){
        return apiIds.get(api);
    }

    public int addDataListener(DataListener dataListener){
        dataLinstener.add(dataListener);
        return dataLinstener.size();
    }

    public int addBtsListener(BtsRpcListener dataListener){
        btsHandles.add(dataListener);
        return btsHandles.size();
    }

    public long call(int apiId , String method , Object[] param){
        ArrayList<Serializable> params = new ArrayList<>(3);
        params.add(0,apiId);
        params.add(1,method);
        params.add(2,param);
        return jRpc.call(RPC.CALL,gson.toJsonTree(params).getAsJsonArray(),btsCallBack);
    }

    public long call(int apiId , String method , List param){
        ArrayList params = new ArrayList<>(3);
        params.add(0,apiId);
        params.add(1,method);
        params.add(2,param);
        return jRpc.call(RPC.CALL,gson.toJsonTree(params).getAsJsonArray(),btsCallBack);
    }

    private class BtsHandle implements JRpc.RpcHandle, JRpc.RpcNoticeHandle {

        @Override
        public void onDisconnect(Throwable throwable) {
            status = STATUS_CLOSED;
            for (BtsRpcListener b:
                    btsHandles) {
                b.onClosed();
            }
        }

        @Override
        public void onConnect() {
            status = STATUS_CONNECTION;
            long id = RPC.login(BtsApi.this,"","");
            callIds.put(id,RPC.CALL_LOGIN);
        }

        @Override
        public void onNotice(RpcNotice notice) {
            for (BtsRpcListener bp:
                btsHandles) {
                bp.onNotice(notice);
            }
        }
    }

    private class BtsCallBack implements JRpc.RpcCallBack{

        @Override
        public void onResult(RpcReturn result) {
            if(result.getError() != null){

            }else{
                long id = result.getId();
                if(RPC.CALL_LOGIN.equals(callIds.get(id))){
                    id = RPC.database(BtsApi.this);
                    callIds.put(id , RPC.CALL_DATABASE);
                }else if(RPC.CALL_DATABASE.equals(callIds.get(id))){
                    apiIds.put(RPC.CALL_DATABASE , result.getResult().getAsInt());
                    id = RPC.history(BtsApi.this);
                    callIds.put(id , RPC.CALL_HISTORY);
                }else if(RPC.CALL_HISTORY.equals(callIds.get(id))){
                    apiIds.put(RPC.CALL_HISTORY , result.getResult().getAsInt());
                    id = RPC.network_broadcast(BtsApi.this);
                    callIds.put(id , RPC.CALL_NETWORK_BROADCAST);
                }else if(RPC.CALL_NETWORK_BROADCAST.equals(callIds.get(id))){
                    apiIds.put(RPC.CALL_NETWORK_BROADCAST , result.getResult().getAsInt());
                    id = RPC.get_chain_id(apiIds.get(RPC.CALL_DATABASE),BtsApi.this);
                    callIds.put(id , RPC.CALL_GET_CHAIN_ID);
                }else if(RPC.CALL_GET_CHAIN_ID.equals(callIds.get(id))){
                    config.sChniaId = result.getResult().getAsString();
                    status = STATUS_CONNECTED;
                    for (BtsRpcListener b:
                         btsHandles) {
                        b.onOpen();
                    }
                    callIds.clear();
                }else{
                    for (DataListener dl:dataLinstener) {
                        dl.onResult(result);
                    }
                }
            }
        }

        @Override
        public void onException(Throwable throwable) {

        }
    }

}
