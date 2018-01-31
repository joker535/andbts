package cn.guye.bts.contorl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.models.AccountOptions;
import cn.guye.bitshares.models.AccountTransactionHistory;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.Authority;
import cn.guye.bitshares.models.BucketObject;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bitshares.models.OperationHistory;
import cn.guye.bts.R;
import cn.guye.bts.app.BtsApp;
import cn.guye.bts.data.DataCenter;
import cn.guye.bitshares.wallet.types;
import cn.guye.tools.jrpclib.JRpcError;
import cn.guye.tools.jrpclib.RpcNotice;
import cn.guye.tools.jrpclib.RpcReturn;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class BtsContorler implements BtsApi.BtsRpcListener, BtsApi.DataListener {

    static {
        Security.addProvider(new BouncyCastleProvider());
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
                .registerTypeAdapter(Authority.class,new Authority.AuthorityDeserializer())
                .registerTypeAdapter(types.public_key_type.class,new types.public_key_type_deserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .registerTypeAdapter(AccountOptions.class,new AccountOptions.AccountOptionsDeserializer())
                .registerTypeAdapter(LimitOrder.class,new LimitOrder.LimitOrderDeserializer())
                .create();
    }
    private static BtsContorler instance;

    public static synchronized BtsContorler getInstance(){
        if(instance == null){
            instance = new BtsContorler();
        }
        return instance;
    }


    public <T> T parse(JsonElement jsonElement,Class<T> tClass){
        return gson.fromJson(jsonElement,tClass);
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

        String[] assets = BtsApp.instance.getResources().getStringArray(R.array.assets);//TODO config

        BtsRequest request = BtsRequestHelper.lookup_asset_symbols(assets, new BtsRequest.CallBack() {
            @Override
            public void onResult(BtsRequest request, JsonElement data) {

                Asset[] assetarry ;
                JsonArray array = data.getAsJsonArray();
                assetarry = new Asset[array.size()];
                Asset.AssetDeserializer deserializer = new Asset.AssetDeserializer();
                for (int i = 0 ;i< assetarry.length ; i++){
                    assetarry[i] = deserializer.deserialize(array.get(i),Asset.class,null);
                }
                dataCenter.addData(assetarry);
                eventBus.post(new BtsConnectEvent(api.getStatus(),"onOpen"));
            }

            @Override
            public void onError(JRpcError error) {

            }
        });
        send(request);
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

    public void addData(Asset[] assetarry) {
        dataCenter.addData(assetarry);
    }

    public GrapheneObject getDataSync(String id){
        return dataCenter.getDataSync(id);
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
