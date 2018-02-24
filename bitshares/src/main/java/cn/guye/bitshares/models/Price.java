package cn.guye.bitshares.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The price struct stores asset prices in the Graphene system.
 *
 */
public class Price {
    public AssetAmount base;
    public AssetAmount quote;

    public BigDecimal base2Quote(Asset b , Asset q){
        try {
            return get_asset_amount(base.getAmount(), b).divide(get_asset_amount(quote.getAmount(), q),b.getPrecision(), RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BigDecimal get_asset_amount(BigDecimal amount, Asset assetObject) {
        if (amount.longValue() == 0) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(amount.toString() +"E-"+assetObject.getPrecision());
        }
    }

    public BigDecimal quote2Base(Asset b , Asset q){
        try {
            return get_asset_amount(quote.getAmount(), q).divide(get_asset_amount(base.getAmount(), b),q.getPrecision(), RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
