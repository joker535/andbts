package cn.guye.bts;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.BucketObject;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.HistoryPrice;
import cn.guye.bitshares.models.Price;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bts.data.DataCenter;
import cn.guye.bts.view.EmptyView;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class MarketFragment extends BaseFragment implements BtsRequest.CallBack ,DataCenter.DataChangeHandler {

    private ListView listView ;
    private Map<String , Asset> assets = new HashMap<>();
    private Map<String , HistoryPrice> prices = new HashMap<>();
    private Map<String , Long> volum = new HashMap<>();
    private MarketAdapter adapter;
    private Asset cny;
    private Handler handler;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matket,container,false);

//        EventBus.getDefault().register(this);

        String[] assets = getContext().getResources().getStringArray(R.array.assets);//TODO config

        BtsRequest request = BtsRequestHelper.lookup_asset_symbols(assets , this);

        BtsContorler.getInstance().send(request);
        BtsContorler.getInstance().regDataChange(this);
        listView = (ListView) (rootView.findViewById(R.id.list_view));
        adapter = new MarketAdapter();
        listView.setAdapter(adapter);

        handler = new Handler();
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

    private HistoryPrice getPrice(BucketObject[] bucketObjects) {
        HistoryPrice prices = new HistoryPrice();
        BucketObject last = bucketObjects[bucketObjects.length-1];

        if(assets.get(last.key.base.getObjectId()) == null || assets.get(last.key.quote.getObjectId()) == null){
            return null;
        }

        prices.high = new Price();
        prices.high.base = new AssetAmount(last.high_base,assets.get(last.key.base.getObjectId()));
        prices.high.quote = new AssetAmount(last.high_quote,assets.get(last.key.quote.getObjectId()));

        prices.low = new Price();
        prices.low.base = new AssetAmount(last.low_base,assets.get(last.key.base.getObjectId()));
        prices.low.quote = new AssetAmount(last.low_quote,assets.get(last.key.quote.getObjectId()));

        prices.open = new Price();
        prices.open.base = new AssetAmount(last.open_base,assets.get(last.key.base.getObjectId()));
        prices.open.quote = new AssetAmount(last.open_quote,assets.get(last.key.quote.getObjectId()));

        prices.close = new Price();
        prices.close.base = new AssetAmount(last.close_base,assets.get(last.key.base.getObjectId()));
        prices.close.quote = new AssetAmount(last.close_quote,assets.get(last.key.quote.getObjectId()));

        prices.volume = new Price();
        prices.volume.base = new AssetAmount(last.base_volume,assets.get(last.key.base.getObjectId()));
        prices.volume.quote = new AssetAmount(last.quote_volume,assets.get(last.key.quote.getObjectId()));

        prices.date = last.key.open;
        return prices;
    }

    @Override
    public void onResult(final BtsRequest request,final JsonElement data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(request.getMethod().equals(RPC.CALL_LOOKUP_ASSET_SYMBOLS)){
                    Asset[] assetarry ;
                    JsonArray array = data.getAsJsonArray();
                    assetarry = new Asset[array.size()];
                    Asset.AssetDeserializer deserializer = new Asset.AssetDeserializer();
                    for (int i = 0 ;i< assetarry.length ; i++){
                        assetarry[i] = deserializer.deserialize(array.get(i),Asset.class,null);
                    }

                    BtsContorler.getInstance().addData(assetarry);
                    List<Asset> uiList = new ArrayList<>(assetarry.length-1);
                    for (Asset a :
                            assetarry) {
                        if (a.getSymbol().equals(getResources().getString(R.string.asset_cny))) {
                            cny = a;
                        }
                        assets.put(a.getObjectId(), a);
                    }

                    for (Asset asset : assets.values()) {
                        if (!asset.getSymbol().equals(getResources().getString(R.string.asset_cny))) {
                            BtsRequest r = BtsRequestHelper.get_market_history(asset.getObjectId(), cny.getObjectId(), 60, new Date(System.currentTimeMillis() - 60 * 60 *1000), new Date(),MarketFragment.this);
                            BtsContorler.getInstance().send(r);
                            uiList.add(asset);
                        }
                    }
                    adapter.assetList =uiList;
                    adapter.notifyDataSetChanged();
                    BtsRequest btsRequest = BtsRequestHelper.set_subscribe_callback(null);

                    BtsContorler.getInstance().send(btsRequest);
                }else if(request.getMethod().equals(RPC.CALL_GET_MARKET_HISTORY)){
                    BucketObject[] bucketObjects ;
                    JsonArray array = data.getAsJsonArray();
                    bucketObjects = new BucketObject[array.size()];
                    BucketObject.BucketDeserializer bucketDeserializer = new BucketObject.BucketDeserializer();
                    for (int i = 0 ;i< bucketObjects.length ; i++){
                        bucketObjects[i] = bucketDeserializer.deserialize(array.get(i),BucketObject.class,null);
                    }

                    if(request.getTag() == null){
                        if(bucketObjects != null && bucketObjects.length != 0){
                            HistoryPrice p = getPrice(bucketObjects);
                            if(p != null){
                                prices.put(p.close.base.getAsset().getObjectId().equals(cny.getObjectId()) ? p.close.quote.getAsset().getObjectId() : p.close.base.getAsset().getObjectId(), p);
                                adapter.notifyDataSetChanged();
                                BtsRequest r = BtsRequestHelper.get_market_history(p.close.base.getAsset().getObjectId().equals(cny.getObjectId()) ? p.close.quote.getAsset().getObjectId() : p.close.base.getAsset().getObjectId(), cny.getObjectId(), 24 * 60 *60, new Date(System.currentTimeMillis() - 2 * 24 * 60 *60 *1000), new Date(),MarketFragment.this);
                                r.setTag("volum");
                                BtsContorler.getInstance().send(r);
                            }
                        }
                    }else{
                        if(bucketObjects != null && bucketObjects.length != 0){
                            HistoryPrice p = getPrice(bucketObjects);
                            if(p != null){
                                volum.put(p.close.base.getAsset().getObjectId().equals(cny.getObjectId()) ? p.close.quote.getAsset().getObjectId() : p.close.base.getAsset().getObjectId(), Price.get_asset_amount(p.volume.quote.getAmount(), assets.get(p.volume.quote.getAsset().getObjectId())).longValue());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            }
        });
    }

    @Override
    public void onError(JRpcError error) {

    }

    @Override
    public void onDataChange(int event, GrapheneObject[] data) {
        if(event == DataCenter.ADD){
            for (GrapheneObject o:
                 data) {
                switch (o.getObjectType()){
                    case BUCKET_OBJECT:
                        HistoryPrice p = getPrice(new BucketObject[]{(BucketObject) o});
                        if(p != null && (p.open.base.getAsset().getObjectId().equals(cny.getObjectId()) || p.open.quote.getAsset().getObjectId().equals(cny.getObjectId()))){
                            if(p != null){
                                prices.put(p.close.base.getAsset().getObjectId().equals(cny.getObjectId()) ? p.close.quote.getAsset().getObjectId() : p.close.base.getAsset().getObjectId(), p);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        }
    }

    private class MarketAdapter extends BaseAdapter{
        private EmptyView emptyView = new EmptyView(getContext());
        public List<Asset> assetList = Collections.EMPTY_LIST;

        @Override
        public int getCount() {
            return assetList.size();
        }

        @Override
        public Object getItem(int position) {
            return assetList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Asset asset = assetList.get(position);
            String message = asset.getSymbol() + " : ";
            HistoryPrice price = prices.get(asset.getObjectId());
            if(price == null){
                message += "--";
            }else{
                Price p = price.close;
                if(p.base.getAsset().getObjectId().equals(cny.getObjectId())){
                    message += p.base2Quote(assets.get(p.base.getAsset().getObjectId()) , assets.get(p.quote.getAsset().getObjectId())).toString();
                    String v = String.valueOf(volum.get(price.volume.quote.getAsset().getObjectId()));
                     message += " 量: " ;
                     message += v==null?"--":v;
                }else{
                    message += p.quote2Base(assets.get(p.base.getAsset().getObjectId()) , assets.get(p.quote.getAsset().getObjectId())).toString();
                    String v = String.valueOf(volum.get(price.volume.base.getAsset().getObjectId()));
                    message += " 量: " ;
                    message += v==null?"--":v;
                }
            }
            TextView textView = new TextView(getActivity());
            textView.setText(message);
            return textView;
        }
    }
}
