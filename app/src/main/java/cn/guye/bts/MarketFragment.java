package cn.guye.bts;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.BucketObject;
import cn.guye.bitshares.models.HistoryPrice;
import cn.guye.bitshares.models.MarketTrade;
import cn.guye.bitshares.models.Price;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.view.EmptyView;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class MarketFragment extends BaseFragment {

    private ListView listView ;
    private Map<String , Asset> assets = new HashMap<>();
    private Map<String , Price> prices = new HashMap<>();
    private MarketAdapter adapter;
    private Asset cny;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matket,container,false);

        EventBus.getDefault().register(this);

        String[] assets = getContext().getResources().getStringArray(R.array.assets);//TODO config
        BtsContorler.getInstance().look_up_assets(assets);
        listView = (ListView) (rootView.findViewById(R.id.list_view));
        adapter = new MarketAdapter();
        listView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventBus(BtsContorler.BtsResultEvent r){
        if(r.error ==null){
            if(r.method.equals(RPC.CALL_LOOKUP_ASSET_SYMBOLS)){
                Asset[] assetarry = ((Asset[]) r.result);
                for (Asset a:
                     assetarry) {
                    if(a.getSymbol().equals(getResources().getString(R.string.asset_cny))){
                        cny = a;
                    }
                    assets.put(a.getObjectId(),a);
                }
                adapter.notifyDataSetChanged();

                for (Asset asset: assets.values()) {
                    if (!asset.getSymbol().equals(getResources().getString(R.string.asset_cny))) {
                        BtsContorler.getInstance().get_market_history(asset.getObjectId(),cny.getObjectId(), 300, new Date(System.currentTimeMillis() - 12*300000) , new Date());
                    }
                }
                BtsContorler.getInstance().set_subscribe_callback();
            }else if(r.method.equals(RPC.CALL_GET_MARKET_HISTORY)){
                BucketObject[] bucketObjects = (BucketObject[]) r.result;
                HistoryPrice[] p = getPrice(bucketObjects);
                for (int i = 0;i<bucketObjects.length;i++)
                    System.out.println("cny - " + assets.get(bucketObjects[i].key.quote.getObjectId().equals(cny.getObjectId())?bucketObjects[i].key.base.getObjectId():bucketObjects[i].key.quote.getObjectId()).getSymbol() + " : " + p[i].close);
                }
            }else if(r.method.equals(RPC.CALL_GET_TRADE_HISTORY)){
                MarketTrade[] marketTrades = (MarketTrade[]) r.result;
            }
        }


    private HistoryPrice[] getPrice(BucketObject[] bucketObjects) {
        HistoryPrice[] prices = new HistoryPrice[bucketObjects.length];
        int i=0;
        for (BucketObject b:
             bucketObjects) {
            prices[i] = priceFromBucket(bucketObjects[i],bucketObjects[i++].key.quote);
        }
        return prices;
    }

    private HistoryPrice priceFromBucket(BucketObject bucket,Asset quoteAsset) {
        HistoryPrice price = new HistoryPrice();
        price.date = bucket.key.open;

        price.high = get_asset_price(bucket.high_base, assets.get(bucket.key.base.getObjectId()),
                bucket.high_quote, assets.get(bucket.key.quote.getObjectId()));
        price.low = get_asset_price(bucket.low_base, assets.get(bucket.key.base.getObjectId()),
                bucket.low_quote, assets.get(bucket.key.quote.getObjectId()));
        price.open = get_asset_price(bucket.open_base, assets.get(bucket.key.base.getObjectId()),
                bucket.open_quote, assets.get(bucket.key.quote.getObjectId()));
        price.close = get_asset_price(bucket.close_base, assets.get(bucket.key.base.getObjectId()),
                bucket.close_quote, assets.get(bucket.key.quote.getObjectId()));
        price.volume = get_asset_amount(bucket.quote_volume, assets.get(bucket.key.quote.getObjectId())).doubleValue();

        if (price.low == 0) {
            price.low = findMin(price.open, price.close);
        }
        if (price.high == Double.NaN || price.high == Double.POSITIVE_INFINITY) {
            price.high = findMax(price.open, price.close);
        }
        if (price.close == Double.POSITIVE_INFINITY || price.close == 0) {
            price.close = price.open;
        }
        if (price.open == Double.POSITIVE_INFINITY || price.open == 0) {
            price.open = price.close;
        }
        if (price.high > 1.3 * ((price.open + price.close) / 2)) {
            price.high = findMax(price.open, price.close);
        }
        if (price.low < 0.7 * ((price.open + price.close) / 2)) {
            price.low = findMin(price.open, price.close);
        }
        return price;
    }

    private static double findMax(double a, double b) {
        if (a != Double.POSITIVE_INFINITY && b != Double.POSITIVE_INFINITY) {
            return Math.max(a, b);
        } else if (a == Double.POSITIVE_INFINITY) {
            return b;
        } else {
            return a;
        }
    }

    private static double findMin(double a, double b) {
        if (a != 0 && b != 0) {
            return Math.min(a, b);
        } else if (a == 0) {
            return b;
        } else {
            return a;
        }
    }

    public static BigDecimal get_scaled_precision(Asset asset) {

        return new BigDecimal("1E-"+asset.getPrecision());
    }

    public static double get_asset_price(BigDecimal quoteAmount, Asset quoteAsset,
                                         BigDecimal baseAmount, Asset baseAsset) {
        if (quoteAsset == null || baseAsset == null) {
            return 1;
        }
        try {
            return get_asset_amount(quoteAmount, quoteAsset).divide(get_asset_amount(baseAmount, baseAsset),quoteAsset.getPrecision(), RoundingMode.DOWN).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }
    public static BigDecimal get_asset_amount(BigDecimal amount, Asset assetObject) {
        if (amount.longValue() == 0) {
            return BigDecimal.ONE;
        } else {
            return new BigDecimal(amount.toString() +"E-"+assetObject.getPrecision());
        }
    }


    private class MarketAdapter extends BaseAdapter{
        private EmptyView emptyView = new EmptyView(getContext());

        @Override
        public int getCount() {
//            return assets.size()-1;
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
