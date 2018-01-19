package cn.guye.bts.contorl;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import cn.guye.tools.jrpclib.JRpcError;
import cn.guye.tools.jrpclib.RpcNotice;
import cn.guye.tools.jrpclib.RpcReturn;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class BtsContorler implements BtsApi.BtsRpcListener, BtsApi.DataListener {

    private DataCenter dataCenter;
    private EventBus eventBus = EventBus.getDefault();
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
            event = new BtsResultEvent(result.getId(),((String)result.getCall().getParams()[1]),param,result.getError());
        }else{
            if(result.getCall().getParams()[1].equals(RPC.CALL_LOOKUP_ASSET_SYMBOLS)){
                Asset[] assets ;
                JsonArray array = result.getResult().getAsJsonArray();
                assets = new Asset[array.size()];
                Asset.AssetDeserializer deserializer = new Asset.AssetDeserializer();
                for (int i = 0 ;i< assets.length ; i++){
                    assets[i] = deserializer.deserialize(array.get(i),Asset.class,null);
                }
                event = new BtsResultEvent(result.getId(),((String)result.getCall().getParams()[1]),param,assets);
            }else if(result.getCall().getParams()[1].equals(RPC.CALL_GET_MARKET_HISTORY)){
                BucketObject[] bucketObjects ;
                JsonArray array = result.getResult().getAsJsonArray();
                bucketObjects = new BucketObject[array.size()];
                BucketObject.BucketDeserializer bucketDeserializer = new BucketObject.BucketDeserializer();
                for (int i = 0 ;i< bucketObjects.length ; i++){
                    bucketObjects[i] = bucketDeserializer.deserialize(array.get(i),BucketObject.class,null);
                }
                event = new BtsResultEvent(result.getId(),((String)result.getCall().getParams()[1]),param,bucketObjects);
            }else if(result.getCall().getParams()[1].equals(RPC.CALL_GET_TRADE_HISTORY)){
                MarketTrade[] marketTrades ;
                JsonArray array = result.getResult().getAsJsonArray();
                marketTrades = new MarketTrade[array.size()];
                Gson gson = new Gson();
                for (int i = 0 ;i< marketTrades.length ; i++){
                    marketTrades[i] = gson.fromJson(array.get(i),MarketTrade.class);
                }
                event = new BtsResultEvent(result.getId(),((String)result.getCall().getParams()[1]),param,marketTrades);
            }else{
                event = null;
            }
        }
        if(event != null){
            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void onError() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"error"));
    }

    @Override
    public void onDataChange() {

    }

    @Override
    public void onNotice(RpcNotice rpcNotice) {
        JsonArray params = rpcNotice.getParams().getAsJsonArray();
        params = params.get(1).getAsJsonArray().get(0).getAsJsonArray();
        List<GrapheneObject> datas = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
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
            }
        }


        eventBus.post(rpcNotice);
    }

    public long look_up_assets(String[] assets) {
        return RPC.lookup_asset_symbols(api,assets);
    }

    public long get_market_history(String base, String quote, long bucket, Date start, Date end){
        return RPC.get_market_history(api, base,quote,bucket,start,end);
    }

    public long get_trade_history( String base, String quote, Date start, Date end, int limit){
        return RPC.get_trade_history(api, base,quote,start,end,limit);
    }

    public long set_subscribe_callback(){
        return RPC.set_subscribe_callback(api);
    }

    public long subscribe_to_market(String base, String quote){
        return RPC.subscribe_to_market(api,base,quote);
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
