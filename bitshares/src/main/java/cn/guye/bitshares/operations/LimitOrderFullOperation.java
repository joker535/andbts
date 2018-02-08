package cn.guye.bitshares.operations;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.guye.bitshares.Util;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.UserAccount;

/**
 * Created by nieyu2 on 18/2/8.
 */

public class LimitOrderFullOperation extends BaseOperation {

    public static final String KEY_SELLER = "account_id";
    public static final String KEY_AMOUNT_TO_SELL = "pays";
    public static final String KEY_MIN_TO_RECEIVE = "receives";

    private String order_id;
    private String account_id;
    private AssetAmount pays;
    private AssetAmount receives;
    private AssetAmount fee; // paid by receiving account

    public LimitOrderFullOperation() {
        super(OperationType.FILL_ORDER_OPERATION);
    }

    public AssetAmount getFee() {
        return fee;
    }

    public String getAccount() {
        return account_id;
    }

    public AssetAmount getPays() {
        return pays;
    }

    public AssetAmount getReceives() {
        return receives;
    }

    public LimitOrderFullOperation(String seller, AssetAmount amountToSell, AssetAmount minToReceive) {
        super(OperationType.FILL_ORDER_OPERATION);
        account_id = seller;
        pays = amountToSell;
        receives = minToReceive;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        fee = assetAmount;
    }

    public static class LimitOrderFullDeserializer implements JsonDeserializer<LimitOrderFullOperation> {

        @Override
        public LimitOrderFullOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json.isJsonArray()){
                // This block is used just to check if we are in the first step of the deserialization
                // when we are dealing with an array.
                JsonArray serializedTransfer = json.getAsJsonArray();
                if(serializedTransfer.get(0).getAsInt() != OperationType.LIMIT_ORDER_CREATE_OPERATION.ordinal()){
                    // If the operation type does not correspond to a transfer operation, we return null
                    return null;
                }else{
                    // Calling itself recursively, this is only done once, so there will be no problems.
                    return context.deserialize(serializedTransfer.get(1), LimitOrderFullOperation.class);
                }
            }else{
                // This block is called in the second recursion and takes care of deserializing the
                // limit order data itself.
                JsonObject jsonObject = json.getAsJsonObject();

                AssetAmount fee = context.deserialize(jsonObject.get(KEY_FEE), AssetAmount.class);
                String seller = jsonObject.get(KEY_SELLER).getAsString();
                AssetAmount amountToSell = context.deserialize(jsonObject.get(KEY_AMOUNT_TO_SELL), AssetAmount.class);
                AssetAmount minToReceive = context.deserialize(jsonObject.get(KEY_MIN_TO_RECEIVE), AssetAmount.class);

                // Creating an instance of the LimitOrderCreateOperation and setting the fee
                LimitOrderFullOperation operation = new LimitOrderFullOperation(seller, amountToSell, minToReceive);
                operation.setFee(fee);
                return operation;
            }
        }
    }
}
