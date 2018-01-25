package cn.guye.bts.contorl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.guye.bitshares.BtsApi;

import static cn.guye.bitshares.RPC.CALL_DATABASE;
import static cn.guye.bitshares.RPC.CALL_GET_ACCOUNT_BY_NAME;
import static cn.guye.bitshares.RPC.CALL_GET_FULL_ACCOUNTS;
import static cn.guye.bitshares.RPC.CALL_GET_MARKET_HISTORY;
import static cn.guye.bitshares.RPC.CALL_GET_PROPOSED_TRANSACTIONS;
import static cn.guye.bitshares.RPC.CALL_GET_TRADE_HISTORY;
import static cn.guye.bitshares.RPC.CALL_HISTORY;
import static cn.guye.bitshares.RPC.CALL_LOGIN;
import static cn.guye.bitshares.RPC.CALL_LOOKUP_ACCOUNT_NAMES;
import static cn.guye.bitshares.RPC.CALL_LOOKUP_ASSET_SYMBOLS;
import static cn.guye.bitshares.RPC.CALL_NETWORK_BROADCAST;
import static cn.guye.bitshares.RPC.CALL_SET_SUBSCRIBE_CALLBACK;
import static cn.guye.bitshares.RPC.CALL_SUBSCRIBE_TO_MARKET;
import static cn.guye.bitshares.RPC.GET_ACCOUNT_BALANCES;

/**
 * Created by nieyu2 on 18/1/22.
 */

public class BtsRequestHelper {

    public static BtsRequest lookup_asset_symbols(String[] ids, BtsRequest.CallBack callBack){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(ids);
        return new BtsRequest(CALL_DATABASE,CALL_LOOKUP_ASSET_SYMBOLS ,params.toArray(), callBack);
    }

    public static BtsRequest get_account_by_name(String api , String name, BtsRequest.CallBack callBack){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(name);
        return new BtsRequest(api,CALL_GET_ACCOUNT_BY_NAME ,params.toArray(), callBack);
    }

    public static BtsRequest set_subscribe_callback( BtsRequest.CallBack callBack){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(Short.MAX_VALUE);
        params.add(Boolean.TRUE);
        return new BtsRequest(CALL_DATABASE,CALL_SET_SUBSCRIBE_CALLBACK ,params.toArray(), callBack);
    }

    public static BtsRequest subscribe_to_market(String api , String base, String quote, BtsRequest.CallBack callBack){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(Short.MAX_VALUE-1);
        params.add(base);
        params.add(quote);
        return new BtsRequest(api,CALL_SUBSCRIBE_TO_MARKET ,params.toArray(), callBack);
    }

    public static BtsRequest get_market_history(String base, String quote, long bucket, Date start, Date end, BtsRequest.CallBack callBack){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(base);
        params.add(quote);
        params.add(bucket);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmtTz);
        params.add(dateFormat.format(start));
        params.add(dateFormat.format(end));
        return new BtsRequest(CALL_HISTORY,CALL_GET_MARKET_HISTORY ,params.toArray(), callBack);
    }

    public static BtsRequest get_trade_history(String api, String base, String quote, Date start, Date end, int limit, BtsRequest.CallBack callBack){
        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmtTz);
        listParams.add(dateFormat.format(start));
        listParams.add(dateFormat.format(end));
        listParams.add(limit);
        return new BtsRequest(api,CALL_GET_TRADE_HISTORY ,listParams.toArray(), callBack);
    }

    public static BtsRequest lookup_account_names(String api,String[] names,BtsRequest.CallBack callBack){
        List<Object> listParams = new ArrayList<>();
        listParams.add(names);
        return new BtsRequest(api,CALL_LOOKUP_ACCOUNT_NAMES ,listParams.toArray(), callBack);
    }

    public static BtsRequest get_account_balances(String api,String id,String[] asssets , BtsRequest.CallBack callBack){
        List<Object> listParams = new ArrayList<>();
        listParams.add(id);
        if(asssets == null){
            listParams.add(new String[]{});
        }else{
            listParams.add(asssets);
        }
        return new BtsRequest(api,GET_ACCOUNT_BALANCES ,listParams.toArray(), callBack);
    }
    public static BtsRequest get_proposed_transactions(String api,String id,BtsRequest.CallBack callBack){
        List<Object> listParams = new ArrayList<>();
        listParams.add(id);

        return new BtsRequest(api,CALL_GET_PROPOSED_TRANSACTIONS ,listParams.toArray(), callBack);
    }

    public static BtsRequest get_full_accounts(String api,String[] ids,boolean isSub , BtsRequest.CallBack callBack){
        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        listParams.add(isSub);
        return new BtsRequest(api,CALL_GET_FULL_ACCOUNTS ,listParams.toArray(), callBack);
    }
}
