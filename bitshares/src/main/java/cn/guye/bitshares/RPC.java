package cn.guye.bitshares;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.guye.bitshares.models.Asset;

public class RPC {
    public static final String CALL = "call";
    public static final String CALL_LOGIN = "login";
    public static final String CALL_NETWORK_BROADCAST = "network_broadcast";
    public static final String CALL_HISTORY = "history";
    public static final String CALL_DATABASE = "database";
    public static final String CALL_ASSET = "asset";
    public static final String CALL_SET_SUBSCRIBE_CALLBACK = "set_subscribe_callback";
    public static final String CALL_CANCEL_ALL_SUBSCRIPTIONS = "cancel_all_subscriptions";
    public static final String CALL_GET_ACCOUNT_BY_NAME = "get_account_by_name";
    public static final String CALL_GET_ACCOUNTS = "get_accounts";
    public static final String CALL_GET_DYNAMIC_GLOBAL_PROPERTIES = "get_dynamic_global_properties";
    public static final String CALL_BROADCAST_TRANSACTION = "broadcast_transaction";
    public static final String CALL_GET_REQUIRED_FEES = "get_required_fees";
    public static final String CALL_GET_KEY_REFERENCES = "get_key_references";
    public static final String CALL_GET_RELATIVE_ACCOUNT_HISTORY = "get_relative_account_history";
    public static final String CALL_LOOKUP_ACCOUNTS = "lookup_accounts";
    public static final String CALL_LIST_ASSETS = "list_assets";
    public static final String GET_OBJECTS = "get_objects";
    public static final String GET_ACCOUNT_BALANCES = "get_account_balances";
    public static final String CALL_LOOKUP_ASSET_SYMBOLS = "lookup_asset_symbols";
    public static final String CALL_GET_BLOCK_HEADER = "get_block_header";
    public static final String CALL_GET_LIMIT_ORDERS = "get_limit_orders";
    public static final String CALL_GET_TRADE_HISTORY = "get_trade_history";
    public static final String CALL_GET_MARKET_HISTORY = "get_market_history";
    public static final String CALL_GET_ALL_ASSET_HOLDERS = "get_all_asset_holders";


    public static long login(BtsApi api , String uid , String pwd){
        String[] param = new String[]{uid,pwd};
        return api.call(1,CALL_LOGIN ,param);
    }

    public static long lookup_asset_symbols(BtsApi api , String[] ids ){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(ids);
        return api.call(api.getApiId(CALL_DATABASE),CALL_LOOKUP_ASSET_SYMBOLS ,params);
    }

    public static long get_account_by_name(BtsApi api , String name){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(name);
        return api.call(api.getApiId(CALL_DATABASE),CALL_GET_ACCOUNT_BY_NAME ,params);
    }
    
    public static long get_market_history(BtsApi api,String base, String quote, long bucket, Date start, Date end){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(base);
        params.add(quote);
        params.add(bucket);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        params.add(dateFormat.format(start));
        params.add(dateFormat.format(end));
        return api.call(api.getApiId(CALL_HISTORY),CALL_GET_MARKET_HISTORY ,params);
    }

    public static long database(BtsApi api) {
        ArrayList<Serializable> params = new ArrayList<>();
        return api.call(1,CALL_DATABASE ,params);
    }

    public static long history(BtsApi api) {
        ArrayList<Serializable> params = new ArrayList<>();
        return api.call(1,CALL_HISTORY ,params);
    }

    public static long network_broadcast(BtsApi api) {
        ArrayList<Serializable> params = new ArrayList<>();
        return api.call(1,CALL_NETWORK_BROADCAST ,params);
    }
}