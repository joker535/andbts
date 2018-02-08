package cn.guye.bitshares.operations;

import com.google.common.primitives.Bytes;
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
import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bitshares.models.UserAccount;

/**
 * Created by nelson on 3/21/17.
 */
public class LimitOrderCancelOperation extends BaseOperation {

    // Constants used in the JSON representation
    public static final String KEY_FEE_PAYING_ACCOUNT = "fee_paying_account";
    public static final String KEY_ORDER_ID = "order";


    public LimitOrderCancelOperation(LimitOrder order, UserAccount feePayingAccount) {
        super(OperationType.LIMIT_ORDER_CANCEL_OPERATION);
        this.order = order;
        this.feePayingAccount = feePayingAccount;
    }

    // Inner fields of a limit order cancel operation
    private AssetAmount fee;
    private UserAccount feePayingAccount;
    private LimitOrder order;

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = (JsonArray) super.toJsonObject();
        JsonObject jsonObject = new JsonObject();
        if(fee != null)
            jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_FEE_PAYING_ACCOUNT, feePayingAccount.getObjectId());
        jsonObject.addProperty(KEY_ORDER_ID, order.getObjectId());
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        array.add(jsonObject);
        return array;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        this.fee = assetAmount;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = this.fee.toBytes();
        byte[] feePayingAccountBytes = this.feePayingAccount.toBytes();
        byte[] orderIdBytes = this.order.toBytes();
        byte[] extensions = this.extensions.toBytes();
        return Bytes.concat(feeBytes, feePayingAccountBytes, orderIdBytes, extensions);
    }

    /**
     * Deserializer used to convert the JSON-formatted representation of a limit_order_create_operation
     * into its java object version.
     *
     * The following is an example of the serialized form of this operation:
     *
     *
     *
     */
    public static class LimitOrderCancelDeserializer implements JsonDeserializer<LimitOrderCancelOperation> {

        @Override
        public LimitOrderCancelOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json.isJsonArray()){
                // This block is used just to check if we are in the first step of the deserialization
                // when we are dealing with an array.
                JsonArray serializedTransfer = json.getAsJsonArray();
                if(serializedTransfer.get(0).getAsInt() != OperationType.LIMIT_ORDER_CREATE_OPERATION.ordinal()){
                    // If the operation type does not correspond to a transfer operation, we return null
                    return null;
                }else{
                    // Calling itself recursively, this is only done once, so there will be no problems.
                    return context.deserialize(serializedTransfer.get(1), LimitOrderCancelOperation.class);
                }
            }else{
                // This block is called in the second recursion and takes care of deserializing the
                // limit order data itself.
                JsonObject jsonObject = json.getAsJsonObject();
                AssetAmount fee = context.deserialize(jsonObject.get(KEY_FEE), AssetAmount.class);
                UserAccount payfee = new UserAccount(jsonObject.get(KEY_FEE_PAYING_ACCOUNT).getAsString());
                LimitOrder order = new LimitOrder(jsonObject.get(KEY_ORDER_ID).getAsString());

                // Creating an instance of the LimitOrderCreateOperation and setting the fee
                LimitOrderCancelOperation operation = new LimitOrderCancelOperation(order,payfee);
                operation.setFee(fee);
                return operation;
            }
        }
    }
}
