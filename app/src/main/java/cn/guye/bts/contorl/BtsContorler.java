package cn.guye.bts.contorl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.AccountTransactionHistory;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.BucketObject;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.MarketTrade;
import cn.guye.bitshares.models.ObjectType;
import cn.guye.bitshares.models.OperationHistory;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bts.data.DataCenter;
import cn.guye.bts.fc.crypto.sha256_object;
import cn.guye.bts.wallet.AccountObject;
import cn.guye.bts.wallet.types;
import cn.guye.tools.jrpclib.JRpcError;
import cn.guye.tools.jrpclib.RpcNotice;
import cn.guye.tools.jrpclib.RpcReturn;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class BtsContorler implements BtsApi.BtsRpcListener, BtsApi.DataListener {

    class wallet_object {
        sha256_object chain_id;
        List<AccountObject> my_accounts = new ArrayList<>();
        ByteBuffer cipher_keys;
        HashMap<String, List<types.public_key_type>> extra_keys = new HashMap<>();
        String ws_server = "";
        String ws_user = "";
        String ws_password = "";

        public void update_account(AccountObject accountObject) {
            boolean bUpdated = false;
            for (int i = 0; i < my_accounts.size(); ++i) {
                if (my_accounts.get(i).id == accountObject.id) {
                    my_accounts.remove(i);
                    my_accounts.add(accountObject);
                    bUpdated = true;
                    break;
                }
            }

            if (bUpdated == false) {
                my_accounts.add(accountObject);
            }
        }
    }

    private DataCenter dataCenter;
    private EventBus eventBus = EventBus.getDefault();

    private ConcurrentHashMap<Long , BtsRequest> requests = new ConcurrentHashMap<>();

    private BtsContorler(){
        dataCenter = new DataCenter();
        api = new BtsApi("wss://bitshares-api.wancloud.io/ws");
        api.addBtsListener(this);
        api.addDataListener(this);
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(OperationHistory.class, new OperationHistory.OperationHistoryDeserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();
    }
    private static BtsContorler instance;

    public static synchronized BtsContorler getInstance(){
        if(instance == null){
            instance = new BtsContorler();
        }
        return instance;
    }


    private BtsApi api;

    private Gson gson;

    public void regDataChange(DataCenter.DataChangeHandler handler){
        dataCenter.addDataHandle(handler);
    }

    public int getStatus(){
        return api.getStatus();
    }

    public void start(){
        api.start();
    }

    @Override
    public void onOpen() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"onOpen"));
    }

    @Override
    public void onFialed() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"fialed"));
    }

    @Override
    public void onClosed() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"closed"));
    }

    @Override
    public void onResult(RpcReturn result) {
        BtsResultEvent event ;
        Object[] param = new Object[result.getCall().getParams().length -2];
        System.arraycopy(result.getCall().getParams(),2,param,0 ,param.length);
        if(result.getError() != null){
            BtsRequest request = requests.get(result.getId());
            if(request != null && request.getCallBack() != null){
                request.getCallBack().onError(result.getError());
            }
            requests.remove(request.getId());

        }else{

            BtsRequest request = requests.get(result.getId());
            if(request != null && request.getCallBack() != null){
                request.getCallBack().onResult(request, result.getResult());
            }
            requests.remove(request.getId());
        }
    }

    @Override
    public void onError() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"error"));
    }

    @Override
    public void onDataChange() {

    }

    public BtsRequest send(BtsRequest btsRequest){
        long id = api.call(api.getApiId(btsRequest.getApi()),btsRequest.getMethod() ,btsRequest.getParams());
        btsRequest.setId(id);
        requests.put(id,btsRequest);
        return btsRequest;
    }

    @Override
    public void onNotice(RpcNotice rpcNotice) {
        JsonArray params = rpcNotice.getParams().getAsJsonArray();
        params = params.get(1).getAsJsonArray().get(0).getAsJsonArray();
        List<GrapheneObject> datas = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            if(params.get(i).isJsonObject()){
                String id = params.get(i).getAsJsonObject().get("id").getAsString();
                GrapheneObject grapheneObject = new GrapheneObject(id);
                switch(grapheneObject.getObjectType()){
                    case ACCOUNT_TRANSACTION_HISTORY_OBJECT:
                        AccountTransactionHistory transactionHistory = gson.fromJson(params.get(i),AccountTransactionHistory.class);
                        datas.add(transactionHistory);
                        break;
                    case OPERATION_HISTORY_OBJECT:
                        OperationHistory operationHistory = gson.fromJson(params.get(i),OperationHistory.class);
                        datas.add(operationHistory);
                        break;
                    case BUCKET_OBJECT:
                        BucketObject bucketObject = new BucketObject.BucketDeserializer().deserialize(params.get(i),BucketObject.class,null);
                        datas.add(bucketObject);
                        break;
                }
            }
        }
        dataCenter.addData(datas);
    }

    public static class BtsConnectEvent{
        public int status;
        public String desc;

        public BtsConnectEvent(int status, String d) {
            this.status = status;
            this.desc = d;
        }
    }

    public static class BtsResultEvent{
        public long id;
        public String method;
        public Object[] param;
        public Object result;
        public JRpcError error;


        public BtsResultEvent(long id, String method, Object[] param, Object result) {
            this.id = id;
            this.method = method;
            this.param = param;
            this.result = result;
        }

        public BtsResultEvent(long id, String method, Object[] param, JRpcError error) {
            this.id = id;
            this.method = method;
            this.param = param;
            this.error = error;
        }
    }
}
