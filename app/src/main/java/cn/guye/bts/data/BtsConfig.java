package cn.guye.bts.data;

import android.content.Context;

import cn.guye.bts.R;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class BtsConfig {
    public String[] getMarket(Context context){
        return context.getResources().getStringArray(R.array.matkets);
    }

    public String[] getAssets(Context context){
        return context.getResources().getStringArray(R.array.assets);
    }

    public String getApi(){
        return "wss://bitshares-api.wancloud.io/ws";
    }

}
