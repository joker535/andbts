package cn.guye.bitshares.models.chain;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cn.guye.bitshares.fc.io.base_encoder;
import cn.guye.bitshares.fc.io.RawType;
import cn.guye.bitshares.models.AccountOptions;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.Authority;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.Memo;

import static cn.guye.bitshares.models.chain.config.GRAPHENE_BLOCKCHAIN_PRECISION;

public class Operations {
    public static final int ID_TRANSER_OPERATION = 0;
    public static final int ID_CREATE_LIMIT_ORDER_OPERATION = 1;
    public static final int ID_CANCEL_LMMIT_ORDER_OPERATION = 2;
    public static final int ID_UPDATE_LMMIT_ORDER_OPERATION = 3;
    public static final int ID_FILL_LMMIT_ORDER_OPERATION = 4;
    public static final int ID_CREATE_ACCOUNT_OPERATION = 5;

    public static operation_id_map operations_map = new operation_id_map();

    @Expose(serialize = false, deserialize = false)
    public int type;

    public static class operation_id_map {
        private HashMap<Integer, Type> mHashId2Operation = new HashMap<>();

        public operation_id_map() {

            mHashId2Operation.put(ID_TRANSER_OPERATION, TransferOperation.class);
            mHashId2Operation.put(ID_CREATE_LIMIT_ORDER_OPERATION, LimitOrderCreateOperation.class);
            mHashId2Operation.put(ID_CANCEL_LMMIT_ORDER_OPERATION, LimitOrderCancelOperation.class);
            mHashId2Operation.put(ID_UPDATE_LMMIT_ORDER_OPERATION, CallOrderUpdateOperation.class);
            mHashId2Operation.put(ID_FILL_LMMIT_ORDER_OPERATION, FillOrderOperation.class);
//            mHashId2Operation.put(ID_CREATE_ACCOUNT_OPERATION, account_create_operation.class);

        }

        public Type getOperationObjectById(int nId) {
            return mHashId2Operation.get(nId);
        }
//        public Type getOperationFeeObjectById(int nId) {
//            return mHashId2OperationFee.get(nId);
//        }
    }

    public interface base_operation {
        List<Authority> get_required_authorities();
        List<String> get_required_active_authorities();
        List<String> get_required_owner_authorities();

        void write_to_encoder(base_encoder baseEncoder);

        long calculate_fee(Object objectFeeParameter);

        void set_fee(AssetAmount fee);

        String fee_payer();

        List<String> get_account_id_list();

        List<String> get_asset_id_list();
    }



    public static class OperationDeserializer implements JsonDeserializer<Operations> {
        @Override
        public Operations deserialize(JsonElement json,
                                          Type typeOfT,
                                          JsonDeserializationContext context) throws JsonParseException {
            int type;
            JsonArray jsonArray = json.getAsJsonArray();

            type = jsonArray.get(0).getAsInt();
            Operations operations = null;
            switch (type){
                case ID_TRANSER_OPERATION:
                    operations = context.deserialize(jsonArray.get(1),TransferOperation.class);
                    operations.type=type;
                    break;
                case ID_CREATE_LIMIT_ORDER_OPERATION:
                    operations = context.deserialize(jsonArray.get(1),LimitOrderCreateOperation.class);
                    operations.type=type;
                    break;
                case ID_CANCEL_LMMIT_ORDER_OPERATION:
                    operations = context.deserialize(jsonArray.get(1),LimitOrderCancelOperation.class);
                    operations.type=type;
                    break;
                case ID_UPDATE_LMMIT_ORDER_OPERATION:
                    operations = context.deserialize(jsonArray.get(1),CallOrderUpdateOperation.class);
                    operations.type=type;
                    break;
                case ID_FILL_LMMIT_ORDER_OPERATION:
                    operations = context.deserialize(jsonArray.get(1),FillOrderOperation.class);
                    operations.type=type;
                    break;
            }

            return operations;
        }
    }

    public static class OperationSerializer implements JsonSerializer<Operations> {

        @Override
        public JsonElement serialize(Operations src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            array.add(src.type);
            if(src instanceof TransferOperation){
                JsonElement object = context.serialize(src,TransferOperation.class);
                array.add(object);
            }else if(src instanceof LimitOrderCreateOperation){
                JsonElement object = context.serialize(src,LimitOrderCreateOperation.class);
                array.add(object);
            }else if(src instanceof LimitOrderCancelOperation){
                JsonElement object = context.serialize(src,LimitOrderCancelOperation.class);
                array.add(object);
            }else if(src instanceof CallOrderUpdateOperation){
                JsonElement object = context.serialize(src,CallOrderUpdateOperation.class);
                array.add(object);
            }

            return array;
        }
    }




    public static class TransferOperation extends Operations implements base_operation{
        public static class fee_parameters_type {
            long fee       = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
            long price_per_kbyte = 10 * GRAPHENE_BLOCKCHAIN_PRECISION; /// only required for large memos.
        };

        public AssetAmount fee;
        public String from;
        public String to;
        public AssetAmount amount;
        public Memo memo;
        public Set extensions;

        public TransferOperation(){
            type = ID_TRANSER_OPERATION;
        }

        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<String> get_required_active_authorities() {
            List<String> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<String> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            RawType rawObject = new RawType();
            baseEncoder.write(rawObject.get_byte_array(fee.getAmount()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(fee.getAsset().getInstanceId()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(new GrapheneObject(from).getInstanceId()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(new GrapheneObject(to).getInstanceId()));
            baseEncoder.write(rawObject.get_byte_array(amount.getAmount()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(amount.getAsset().getInstanceId()));
            baseEncoder.write(rawObject.get_byte(memo != null));
            if (memo != null) {
                baseEncoder.write(memo.from.getPublicKey().toBytes());
                baseEncoder.write(memo.to.getPublicKey().toBytes());
                baseEncoder.write(rawObject.get_byte_array(memo.nonce));
                byte[] byteMessage = memo.message;
                rawObject.pack(baseEncoder, BigDecimal.valueOf(byteMessage.length));
                baseEncoder.write(byteMessage);
            }

            baseEncoder.write(rawObject.get_byte_array(extensions.size()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(extensions.size()));

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;


            return calculate_fee(feeParametersType);
        }

        @Override
        public void set_fee(AssetAmount assetFee) {
            fee = assetFee;
        }

        @Override
        public String fee_payer() {
            return from;
        }

        @Override
        public List<String> get_account_id_list() {
            List<String> listAccountId = new ArrayList<>();
            listAccountId.add(from);
            listAccountId.add(to);
            return listAccountId;
        }

        @Override
        public List<String> get_asset_id_list() {
            List<String> listAssetId = new ArrayList<>();
            listAssetId.add(amount.getAsset().getObjectId());
            return listAssetId;
        }

        public long calculate_fee(fee_parameters_type feeParametersType) {
            long lFee = feeParametersType.fee;
//            if (memo != null) {
//                // 计算数据价格
//
//                BigInteger nSize = BigInteger.valueOf(new Gson().toJson(memo).length());
//                BigInteger nPrice = BigInteger.valueOf(feeParametersType.price_per_kbyte);
//                BigInteger nKbyte = BigInteger.valueOf(1024);
//                BigInteger nAmount = nPrice.multiply(nSize).divide(nKbyte);
//
//                lFee += nAmount.longValue();
//            }

            return lFee;
        }


    }

    public static class LimitOrderCreateOperation extends Operations implements base_operation{
        static class fee_parameters_type {
            long fee = 5 * GRAPHENE_BLOCKCHAIN_PRECISION;
        }
        public AssetAmount fee;
        public String seller;
        public AssetAmount amount_to_sell;
        public AssetAmount min_to_receive;


        public LimitOrderCreateOperation(){
            type = ID_CREATE_LIMIT_ORDER_OPERATION;
        }

        /// The order will be removed from the books if not filled by expiration
        /// Upon expiration, all unsold asset will be returned to seller
        public Date expiration; // = time_point_sec::maximum();

        /// If this flag is set the entire order must be filled or the operation is rejected
        public boolean fill_or_kill = false;
        public Set extensions = Collections.EMPTY_SET;


        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<String> get_required_active_authorities() {
            List<String> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<String> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            RawType rawObject = new RawType();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.getAmount()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(fee.getAsset().getInstanceId()));

            // seller
            rawObject.pack(baseEncoder, BigDecimal.valueOf(new GrapheneObject(seller).getInstanceId()));

            // amount_to_sell
            baseEncoder.write(rawObject.get_byte_array(amount_to_sell.getAmount()));
            rawObject.pack(baseEncoder,
                    BigDecimal.valueOf(amount_to_sell.getAsset().getInstanceId()));

            // min_to_receive
            baseEncoder.write(rawObject.get_byte_array(min_to_receive.getAmount()));
            rawObject.pack(baseEncoder,
                    BigDecimal.valueOf(min_to_receive.getAsset().getInstanceId()));

            // expiration
            baseEncoder.write(rawObject.get_byte_array(expiration));

            // fill_or_kill
            baseEncoder.write(rawObject.get_byte(fill_or_kill));

            // extensions
            rawObject.pack(baseEncoder,  BigDecimal.valueOf(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(AssetAmount fee) {
            this.fee = fee;
        }

        @Override
        public String fee_payer() {
            return seller;
        }

        @Override
        public List<String> get_account_id_list() {
            List<String> listAccountId = new ArrayList<>();
            listAccountId.add(seller);
            return listAccountId;
        }

        @Override
        public List<String> get_asset_id_list() {
            List<String> listAssetId = new ArrayList<>();
            listAssetId.add(amount_to_sell.getAsset().getObjectId());
            listAssetId.add(min_to_receive.getAsset().getObjectId());
            return listAssetId;
        }


    }

    public static class LimitOrderCancelOperation extends Operations implements base_operation{
        class fee_parameters_type {
            long fee = 0;
        };

        public LimitOrderCancelOperation() {
            type = ID_CANCEL_LMMIT_ORDER_OPERATION;
        }

        public AssetAmount fee;
        public String order;
        /**
         * must be order->seller
         */
        public String fee_paying_account;
        public Set extensions;

        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<String> get_required_active_authorities() {
            List<String> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<String> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            RawType rawObject = new RawType();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.getAmount()));
            rawObject.pack(baseEncoder, BigDecimal.valueOf(fee.getAsset().getInstanceId()));

            // fee_paying_account
            rawObject.pack(baseEncoder,
                    BigDecimal.valueOf(new GrapheneObject(fee_paying_account).getInstanceId()));

            // order
            rawObject.pack(baseEncoder, BigDecimal.valueOf(new GrapheneObject(order).getInstanceId()));

            // extensions
            rawObject.pack(baseEncoder, BigDecimal.valueOf(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(AssetAmount fee) {
            this.fee = fee;
        }

        @Override
        public String fee_payer() {
            return fee_paying_account;
        }

        @Override
        public List<String> get_account_id_list() {
            List<String> listAccountId = new ArrayList<>();
            listAccountId.add(fee_paying_account);
            return listAccountId;
        }

        @Override
        public List<String> get_asset_id_list() {
            List<String> listAssetId = new ArrayList<>();
            return listAssetId;
        }


    }

    public static class CallOrderUpdateOperation extends Operations implements base_operation{
        /**
         * this is slightly more expensive than limit orders, this pricing impacts prediction markets
         */
        class fee_parameters_type {
            long fee = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
        };

        public CallOrderUpdateOperation() {
            type = ID_UPDATE_LMMIT_ORDER_OPERATION;
        }

        AssetAmount fee;
        String funding_account; ///< pays fee, collateral, and cover
        AssetAmount delta_collateral; ///< the amount of collateral to add to the margin position
        AssetAmount delta_debt; ///< the amount of the debt to be paid off, may be negative to issue new debt
        Set extensions;
        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<String> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<String> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(AssetAmount fee) {

        }

        @Override
        public String fee_payer() {
            return funding_account;
        }

        @Override
        public List<String> get_account_id_list() {
            List<String> listAccountId = new ArrayList<>();
            listAccountId.add(funding_account);
            return listAccountId;
        }

        @Override
        public List<String> get_asset_id_list() {
            List<String> listAssetId = new ArrayList<>();
            listAssetId.add(delta_collateral.getAsset().getObjectId());
            listAssetId.add(delta_debt.getAsset().getObjectId());
            return listAssetId;
        }

    }

    public static class FillOrderOperation extends Operations{

        public String order_id;
        public String account_id;
        public AssetAmount pays;
        public AssetAmount receives;
        public AssetAmount fee; // paid by receiving account

    }


    public static class AccountCreateOperation extends Operations implements base_operation {
        class fee_parameters_type {
            long basic_fee       = 5*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            long premium_fee     = 2000*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            int  price_per_kbyte = GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        public AssetAmount fee;
        public String registrar;
        public String referrer;
        public int referrer_percent;
        public String name;
        public Authority owner;
        public Authority active;
        public AccountOptions options;

        public AccountCreateOperation() {
            type = ID_CREATE_ACCOUNT_OPERATION;
        }

        public long calculate_fee(fee_parameters_type feeParametersType) {
//            long lFeeRequired = feeParametersType.basic_fee;
//            if (utils.is_cheap_name(name) == false) {
//                lFeeRequired = feeParametersType.premium_fee;
//            }

            // // TODO: 07/09/2017  未完成
            return 0;

        }

        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<String> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<String> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(AssetAmount fee) {

        }

        @Override
        public String fee_payer() {
            return registrar;
        }

        @Override
        public List<String> get_account_id_list() {
            List<String> listAccountId = new ArrayList<>();
            listAccountId.add(registrar);
            listAccountId.add(referrer);

            return listAccountId;
        }

        @Override
        public List<String> get_asset_id_list() {
            List<String> listAssetId = new ArrayList<>();
            return listAssetId;
        }

    }


}
