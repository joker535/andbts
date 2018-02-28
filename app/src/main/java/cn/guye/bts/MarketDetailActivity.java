package cn.guye.bts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import cn.guye.bitshares.models.Asset;

import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/2/23.
 */

public class MarketDetailActivity extends AppCompatActivity implements BtsRequest.CallBack {
    private LineChart chart;
    private String quoteId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quoteId = getIntent().getStringExtra("id");
        setContentView(R.layout.activity_market_detail);
        chart = (LineChart) findViewById(R.id.chart);
        BtsRequest r = BtsRequestHelper.get_limit_orders("1.3.113",quoteId,300,this);
        BtsContorler.getInstance().send(r);

    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
        if(request.getTag() == null){
            LimitOrder[] ls = BtsContorler.getInstance().parse(data, LimitOrder[].class);
            Asset as = (Asset) BtsContorler.getInstance().getDataSync("1.3.113");
            Asset bas = (Asset) BtsContorler.getInstance().getDataSync(quoteId);
            Map<BigDecimal,Long> askDepth = new HashMap<>();
            Map<BigDecimal,Long> bidDepth = new HashMap<>();


            long asksum = 0;
            for (int i = 0 ; i<ls.length;i++){
                if(ls[i].getSellPrice().base.getAsset().getObjectId().equals(as.getObjectId())){
                    asksum+=ls[i].getSellPrice().quote.getBalance(bas).longValue();
                    BigDecimal bigDecimals = ls[i].getSellPrice().base2Quote(as,bas);
//            bigDecimals = bigDecimals.setScale(2,BigDecimal.ROUND_DOWN);

                    Long l = askDepth.get(bigDecimals);
                    if(l == null){
                        long ll = ls[i].getSellPrice().quote.getBalance(bas).longValue();
                        askDepth.put(bigDecimals,ll);
                    }else{
                        l += ls[i].getSellPrice().quote.getBalance(bas).longValue();
                        askDepth.put(bigDecimals,l);
                    }
                }else{

                    BigDecimal bigDecimals = ls[i].getSellPrice().quote2Base(bas,as);
//            bigDecimals = bigDecimals.setScale(2,BigDecimal.ROUND_DOWN);
                    Long l = bidDepth.get(bigDecimals);
                    if(l == null){
                        long ll = ls[i].getSellPrice().base.getBalance(bas).longValue();
                        bidDepth.put(bigDecimals,ll);
                    }else{
                        l += ls[i].getSellPrice().base.getBalance(bas).longValue();
                        bidDepth.put(bigDecimals,l);
                    }
                }

            }

            Set<BigDecimal> set = askDepth.keySet();
            List<BigDecimal> list = new ArrayList<>(set);
            Collections.sort(list);
            List<Entry> askEntries = new ArrayList<Entry>();
            List<String> xVar = new ArrayList<>();
            long current = asksum;
            int i=0;
            for (BigDecimal bdata : list) {
                current -= askDepth.get(bdata);
                askEntries.add(new Entry(current,i++));
                xVar.add(bdata.toPlainString());
            }


            set = bidDepth.keySet();
            list = new ArrayList<>(set);
            Collections.sort(list);
            List<Entry> bidEntries = new ArrayList<Entry>();
            current = 0;
            for (BigDecimal bdata : list) {
                current += bidDepth.get(bdata);
                askEntries.add(new Entry(current,i++));
                xVar.add(bdata.toPlainString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    LineDataSet askDataSet = new LineDataSet(askEntries, "ask");
                    askDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    askDataSet.setDrawCircles(false);
                    askDataSet.setDrawFilled(true);
                    askDataSet.setColor(0xffff0000);
                    askDataSet.setValueTextColor(0xffff0000);

                    LineDataSet bidDataSet = new LineDataSet(bidEntries, "bid");
                    bidDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    bidDataSet.setDrawCircles(false);
                    bidDataSet.setDrawFilled(true);
                    bidDataSet.setColor(0xff00ff00);
                    bidDataSet.setValueTextColor(0xff00ff00);
                    bidDataSet.setFillColor(0x5500ff22);
                    LineData lineData = new LineData();
                    lineData.addDataSet(askDataSet);
                    lineData.addDataSet(bidDataSet);
                    lineData.setXVals(xVar);
                    chart.setData(lineData);

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    YAxis yAxis = chart.getAxisRight();
                    yAxis.setEnabled(false);

                    chart.invalidate(); // refresh
                }
            });

        }

    }

    @Override
    public void onError(JRpcError error) {

    }
}
