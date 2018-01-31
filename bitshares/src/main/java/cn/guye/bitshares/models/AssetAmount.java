package cn.guye.bitshares.models;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import cn.guye.bitshares.Util;
import cn.guye.bitshares.errors.IncompatibleOperation;


/**
 * Created by nelson on 11/7/16.
 */
public class AssetAmount implements ByteSerializable, JsonSerializable {
    /**
     * Constants used in the JSON serialization procedure.
     */
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_ASSET_ID = "asset_id";

    private BigDecimal amount;
    private GrapheneObject asset;

    public AssetAmount(BigDecimal amount, GrapheneObject asset){
        this.amount = amount;
        this.asset = asset;
    }

    /**
     * Adds two asset amounts. They must refer to the same Asset type.
     * @param other: The other AssetAmount to add to this.
     * @return: A new instance of the AssetAmount class with the combined amount.
     */
    public AssetAmount add(AssetAmount other){
        if(!this.getAsset().getObjectId().equals(other.getAsset().getObjectId())){
            throw new IncompatibleOperation("Cannot add two AssetAmount instances that refer to different assets");
        }
        BigDecimal combined = this.amount.add(other.getAmount());
        return new AssetAmount(combined, asset);
    }

    /**
     * Adds an aditional amount of base units to this AssetAmount.
     * @param additional: The amount to add.
     * @return: A new instance of the AssetAmount class with the added aditional.
     */
    public AssetAmount add(long additional){
        BigDecimal combined = this.amount.add(BigDecimal.valueOf(additional));
        return new AssetAmount(combined, asset);
    }

//    /**
//     * Subtracts another instance of AssetAmount from this one. This method will always
//     * return absolute values.
//     * @param other: The other asset amount to subtract from this.
//     * @return: The absolute value of the subtraction of the other minus this asset amount.
//     */
//    public AssetAmount subtract(AssetAmount other){
//        if(!this.getAsset().getObjectId().equals(other.getAsset().getObjectId())){
//            throw new IncompatibleOperation("Cannot subtract two AssetAmount instances that refer to different assets");
//        }
//        UnsignedLong result = null;
//        if(this.amount.compareTo(other.getAmount()) < 0){
//            result = other.getAmount().minus(this.amount);
//        }else{
//            result = this.amount.minus(other.getAmount());
//        }
//        return new AssetAmount(result, asset);
//    }

    /**
     * Multiplies the current amount by a factor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     * @param factor: The multiplying factor
     * @return The same AssetAmount instance, but with the changed amount value.
     */
    public AssetAmount multiplyBy(BigDecimal factor){
        this.amount = amount.multiply(factor);
        return this;
    }



    public void setAmount(BigDecimal amount){
        this.amount = amount;
    }

    public BigDecimal getAmount(){
        return this.amount;
    }

    public GrapheneObject getAsset(){ return this.asset; }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(byteArrayOutputStream);
        try {
            Varint.writeUnsignedVarLong(asset.instance, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Getting asset id
        byte[] assetId = byteArrayOutputStream.toByteArray();
        byte[] value = Util.revertLong(this.amount.longValue());

        // Concatenating and returning value + asset id
        return Bytes.concat(value, assetId);
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmountSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonAmount = new JsonObject();
        jsonAmount.addProperty(KEY_AMOUNT, amount);
        jsonAmount.addProperty(KEY_ASSET_ID, asset.getObjectId());
        return jsonAmount;
    }

    /**
     * Custom serializer used to translate this object into the JSON-formatted entry we need for a transaction.
     */
    public static class AssetAmountSerializer implements JsonSerializer<AssetAmount> {

        @Override
        public JsonElement serialize(AssetAmount assetAmount, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_AMOUNT, assetAmount.amount);
            obj.addProperty(KEY_ASSET_ID, assetAmount.asset.getObjectId());
            return obj;
        }
    }

    /**
     * Custom deserializer used for this class
     */
    public static class AssetAmountDeserializer implements JsonDeserializer<AssetAmount> {

        @Override
        public AssetAmount deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Long amount = json.getAsJsonObject().get(KEY_AMOUNT).getAsLong();
            String assetId = json.getAsJsonObject().get(KEY_ASSET_ID).getAsString();
            AssetAmount assetAmount = new AssetAmount(BigDecimal.valueOf(amount), new Asset(assetId));
            return assetAmount;
        }
    }
}
