package cn.guye.bts;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.RPC;
import cn.guye.bitshares.errors.MalformedAddressException;
import cn.guye.bitshares.models.Address;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bitshares.wallet.AccountObject;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.types;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class WalletFragment extends BaseFragment implements BtsRequest.CallBack {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().register(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
        if(request.getMethod().equals(RPC.CALL_GET_ACCOUNT_BY_NAME)){
            AccountObject my = BtsContorler.getInstance().parse(data,AccountObject.class);

            BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE,new String[]{"joker53535"},false,this);
            BtsContorler.getInstance().send(r);

        }else{
            FullAccountObject fullAccountObject = BtsContorler.getInstance().parse(data.getAsJsonArray().get(0).getAsJsonArray().get(1),FullAccountObject.class);

            System.out.println(fullAccountObject.account.active.getKeyAuthList().get(0).getAddress());
        }
    }

    @Override
    public void onError(JRpcError error) {

    }


    @Subscribe
    public void onBtsEvent(BtsContorler.BtsConnectEvent e){
        if(e.status == BtsApi.STATUS_CONNECTED){
            BtsRequest btsRequest = BtsRequestHelper.get_account_by_name(RPC.CALL_DATABASE , "joker53535",this);
            BtsContorler.getInstance().send(btsRequest);
        }
    }
}
