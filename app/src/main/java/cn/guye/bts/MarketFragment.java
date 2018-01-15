package cn.guye.bts;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.guye.bitshares.BtsApi;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.tools.jrpclib.RpcReturn;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class MarketFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matket,container,false);

        EventBus.getDefault().register(this);

        String[] assets = getContext().getResources().getStringArray(R.array.assets);//TODO config
        BtsContorler.getInstance().look_up_assets(assets);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventBus(RpcReturn r){
        System.out.println(new Gson().toJson(r));

    }
}
