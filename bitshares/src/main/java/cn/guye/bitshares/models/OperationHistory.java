package cn.guye.bitshares.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cn.guye.bitshares.models.chain.Operations;

/**
 * Created by nieyu2 on 18/1/18.
 */

//     ex: {
//         "id": "1.11.119742831",
//         "op": [
//         4,
//         {
//         "fee": {
//         "amount": 5540,
//         "asset_id": "1.3.1570"
//         },
//         "order_id": "1.7.48353501",
//         "account_id": "1.2.643261",
//         "pays": {
//         "amount": 28126588,
//         "asset_id": "1.3.2888"
//         },
//         "receives": {
//         "amount": 2770469,
//         "asset_id": "1.3.1570"
//         }
//         }
//         ],
//         "result": [
//         0,
//         {
//
//         }
//         ],
//         "block_num": 23634443,
//         "trx_in_block": 7,
//         "op_in_trx": 0,
//         "virtual_op": 46069
//         },


public class OperationHistory extends GrapheneObject{
    public long block_num;
    public int trx_in_block;
    public int op_in_trx;
    public long virtual_op;
    public Operations op;


    public OperationHistory(String id) {
        super(id);
    }

    public static class OperationHistoryDeserializer implements JsonDeserializer<OperationHistory> {

        @Override
        public OperationHistory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String id = json.getAsJsonObject().get("id").getAsString();
            OperationHistory operationHistory = new OperationHistory(id);
            operationHistory.block_num = json.getAsJsonObject().get("block_num").getAsLong();
            operationHistory.trx_in_block = json.getAsJsonObject().get("trx_in_block").getAsInt();
            operationHistory.op_in_trx = json.getAsJsonObject().get("op_in_trx").getAsInt();
            operationHistory.virtual_op = json.getAsJsonObject().get("virtual_op").getAsLong();
            operationHistory.op = new Operations.OperationDeserializer().deserialize(json.getAsJsonObject().get("op"),Operations.class,context);

            return operationHistory;
        }
    }
}
