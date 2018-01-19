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

    public BigDecimal base2Quote(){
        try {
            return get_asset_amount(base.getAmount(), base.getAsset()).divide(get_asset_amount(quote.getAmount(), quote.getAsset()),quote.getAsset().getPrecision(), RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BigDecimal get_asset_amount(BigDecimal amount, Asset assetObject) {
        if (amount.longValue() == 0) {
            return BigDecimal.ONE;
        } else {
            return new BigDecimal(amount.toString() +"E-"+assetObject.getPrecision());
        }
    }

    public BigDecimal quote2Base(){
        try {
            return get_asset_amount(quote.getAmount(), quote.getAsset()).divide(get_asset_amount(base.getAmount(), base.getAsset()),base.getAsset().getPrecision(), RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
