package cn.guye.bitshares.models.chain;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.Memo;

public class Operations {
    public static final int ID_TRANSER_OPERATION = 0;
    public static final int ID_CREATE_LIMIT_ORDER_OPERATION = 1;
    public static final int ID_CANCEL_LMMIT_ORDER_OPERATION = 2;
    public static final int ID_UPDATE_LMMIT_ORDER_OPERATION = 3;
    public static final int ID_FILL_LMMIT_ORDER_OPERATION = 4;
    public static final int ID_CREATE_ACCOUNT_OPERATION = 5;

    public static operation_id_map operations_map = new operation_id_map();

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



    public static class TransferOperation extends Operations{


        public AssetAmount fee;
        public String from;
        public String to;
        public AssetAmount amount;
//        public Memo memo; //TODO
        public Set extensions;


    }

    public static class LimitOrderCreateOperation extends Operations{

        public AssetAmount fee;
        public String seller;
        public AssetAmount amount_to_sell;
        public AssetAmount min_to_receive;

        /// The order will be removed from the books if not filled by expiration
        /// Upon expiration, all unsold asset will be returned to seller
        public Date expiration; // = time_point_sec::maximum();

        /// If this flag is set the entire order must be filled or the operation is rejected
        public boolean fill_or_kill = false;
        public Set extensions;


    }

    public static class LimitOrderCancelOperation extends Operations{


        public AssetAmount fee;
        public String order;
        /**
         * must be order->seller
         */
        public String fee_paying_account;
        public Set extensions;


    }

    public static class CallOrderUpdateOperation extends Operations{
        /**
         * this is slightly more expensive than limit orders, this pricing impacts prediction markets
         */

        AssetAmount fee;
        String funding_account; ///< pays fee, collateral, and cover
        AssetAmount delta_collateral; ///< the amount of collateral to add to the margin position
        AssetAmount delta_debt; ///< the amount of the debt to be paid off, may be negative to issue new debt
        Set extensions;


    }

    public static class FillOrderOperation extends Operations{

        public String order_id;
        public String account_id;
        public AssetAmount pays;
        public AssetAmount receives;
        public AssetAmount fee; // paid by receiving account

    }


}
