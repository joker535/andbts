package cn.guye.bts.contorl;



import org.greenrobot.eventbus.EventBus;

import cn.guye.bitshares.BtsApi;
import cn.guye.bts.data.DataCenter;
import cn.guye.tools.jrpclib.RpcNotice;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class BtsContorler implements BtsApi.BtsRpcListener {

    private DataCenter dataCenter;
    private EventBus eventBus = EventBus.getDefault();
    private BtsContorler(){
        dataCenter = new DataCenter();
        api = new BtsApi("wss://bitshares-api.wancloud.io/ws");
        api.addBtsListener(this);
    }
    private static BtsContorler instance;

    public static synchronized BtsContorler getInstance(){
        if(instance == null){
            instance = new BtsContorler();
        }
        return instance;
    }


    private BtsApi api;


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
    public void onError() {
        eventBus.post(new BtsConnectEvent(api.getStatus(),"error"));
    }

    @Override
    public void onNotice(RpcNotice rpcNotice) {
        eventBus.post(rpcNotice);
    }

    public static class BtsConnectEvent{
        public int status;
        public String desc;

        public BtsConnectEvent(int status, String d) {
            this.status = status;
            this.desc = d;
        }
    }
}
