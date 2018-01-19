package cn.guye.bitshares;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    public static final String CALL_SUBSCRIBE_TO_MARKET = "subscribe_to_market";


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

    public static long set_subscribe_callback(BtsApi api ){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(Short.MAX_VALUE);
        params.add(Boolean.TRUE);
        return api.call(api.getApiId(CALL_DATABASE),CALL_SET_SUBSCRIBE_CALLBACK ,params);
    }

    public static long subscribe_to_market(BtsApi api , String base, String quote){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(Short.MAX_VALUE-1);
        params.add(base);
        params.add(quote);
        return api.call(api.getApiId(CALL_DATABASE),CALL_SUBSCRIBE_TO_MARKET ,params);
    }
    
    public static long get_market_history(BtsApi api,String base, String quote, long bucket, Date start, Date end){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(base);
        params.add(quote);
        params.add(bucket);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmtTz);
        params.add(dateFormat.format(start));
        params.add(dateFormat.format(end));
        return api.call(api.getApiId(CALL_HISTORY),CALL_GET_MARKET_HISTORY ,params);
    }

    public static long get_trade_history(BtsApi api, String base, String quote, Date start, Date end, int limit){
        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmtTz);
        listParams.add(dateFormat.format(start));
        listParams.add(dateFormat.format(end));
        listParams.add(limit);
        return api.call(api.getApiId(CALL_DATABASE),CALL_GET_TRADE_HISTORY ,listParams);
    }


    public static long database(BtsApi api) {
        String[] param = new String[0];
        return api.call(1,CALL_DATABASE ,param);
    }

    public static long history(BtsApi api) {
        String[] param = new String[0];
        return api.call(1,CALL_HISTORY ,param);
    }

    public static long network_broadcast(BtsApi api) {
        String[] param = new String[0];
        return api.call(1,CALL_NETWORK_BROADCAST ,param);
    }
}