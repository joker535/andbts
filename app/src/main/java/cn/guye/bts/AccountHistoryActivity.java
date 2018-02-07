package cn.guye.bts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.OperationHistory;
import cn.guye.bitshares.models.Price;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.operations.OperationType;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/2/7.
 */

public class AccountHistoryActivity extends AppCompatActivity implements BtsRequest.CallBack {
    private ListView list;
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_history);
        
        list = (ListView) findViewById(R.id.list_view);

        adapter = new MyAdapter();

        list.setAdapter(adapter);

        String id = getIntent().getStringExtra("id");

        BtsRequest request = BtsRequestHelper.get_account_history(id , null,null,50,this);

        BtsContorler.getInstance().send(request);
    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
        final ArrayList<OperationHistory> list = new ArrayList<>();
        JsonArray array = data.getAsJsonArray();
        for (JsonElement j : array){
            OperationHistory op = BtsContorler.getInstance().parse(j,OperationHistory.class);
            if(op.op.type == OperationType.FILL_ORDER_OPERATION.ordinal()){
                list.add(op);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.histories = list;
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onError(JRpcError error) {

    }


    private class MyAdapter extends BaseAdapter{

        private List<OperationHistory> histories = Collections.EMPTY_LIST;

        @Override
        public int getCount() {
            return histories.size();
        }

        @Override
        public Object getItem(int position) {
            return histories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(AccountHistoryActivity.this);
            Operations.FillOrderOperation fo = (Operations.FillOrderOperation) histories.get(position).op;

            Asset base = (Asset) BtsContorler.getInstance().getDataSync(fo.pays.getAsset().getObjectId());
            Asset quote = (Asset) BtsContorler.getInstance().getDataSync(fo.receives.getAsset().getObjectId());
            String bName = base == null ? fo.pays.getAsset().getObjectId() : base.getSymbol();
            String qName = quote == null ? fo.receives.getAsset().getObjectId() : quote.getSymbol();
            String amount = base == null ? "-" : Price.get_asset_amount(fo.pays.getAmount(), base).toString();
            StringBuilder sb = new StringBuilder();
            Price p = new Price();
            p.base = fo.pays;
            p.quote = fo.receives;
            sb.append(bName).append("/").append(qName).append(" : ").append(p.base2Quote(base, quote)).append(" for sell:").append(amount).append(bName).append("\n");
            textView.setText(sb.toString());
            return textView;
        }
    }
}
