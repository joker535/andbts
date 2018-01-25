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
    public static final String CALL_LOOKUP_ACCOUNT_NAMES = "lookup_account_names";
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
    public static final String CALL_GET_PROPOSED_TRANSACTIONS = "get_proposed_transactions";
    public static final String CALL_GET_FULL_ACCOUNTS = "get_full_accounts";


    public static long login(BtsApi api , String uid , String pwd){
        String[] param = new String[]{uid,pwd};
        return api.call(1,CALL_LOGIN ,param);
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