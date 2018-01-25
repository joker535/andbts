package cn.guye.bitshares.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.wallet.AccountObject;

/**
 * Created by nieyu2 on 18/1/24.
 */

public class FullAccountObject {

    public AccountObject account;
    public String  registrar_name;
    public String  referrer_name;
    public String  lifetime_referrer_name;

    public LimitOrder[] limit_orders;
//    public Operations[] call_orders;
//    public Operations[] settle_orders;

    public Balances[] balances;


    //TODO others

    public static class Balances extends GrapheneObject{

        public String owner;
        public String asset_type;
        public BigDecimal balance;

        public Balances(String id) {
            super(id);
        }
    }
}
