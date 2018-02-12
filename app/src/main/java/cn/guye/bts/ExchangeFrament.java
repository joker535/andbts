package cn.guye.bts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.Transaction;
import cn.guye.bitshares.models.UserAccount;
import cn.guye.bitshares.models.chain.BlockData;
import cn.guye.bitshares.models.chain.dynamic_global_property_object;
import cn.guye.bitshares.operations.BaseOperation;
import cn.guye.bitshares.operations.LimitOrderCreateOperation;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bts.contorl.MyWallet;
import cn.guye.bts.view.CustomDialog;
import cn.guye.tools.jrpclib.JRpcError;
import okhttp3.Call;

import static org.spongycastle.asn1.x500.style.RFC4519Style.o;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class ExchangeFrament extends BaseFragment implements View.OnClickListener, TextWatcher {

    private View selectCoin;
    private View selectBuy;
    private TextView targetBalacne;
    private TextView baseBalacne;
    private TextView isBuyText;

    private EditText targetCount;
    private EditText baseCount;

    private EditText price;

    private View buy;

    private String selectAsset;
    private boolean isBuy = true;
    private Asset baseAsset;
    private Asset targetAsset;

    private CustomDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exchange,container,false);
        selectCoin = rootView.findViewById(R.id.select_cion);
        selectCoin.setOnClickListener(this);
        selectBuy = rootView.findViewById(R.id.select_buy);
        selectBuy.setOnClickListener(this);

        targetBalacne = rootView.findViewById(R.id.targetbalance);
        baseBalacne = rootView.findViewById(R.id.basebalance);

        targetCount = rootView.findViewById(R.id.targetcount);
        targetCount.addTextChangedListener(this);
        baseCount = rootView.findViewById(R.id.basecount);

        price = rootView.findViewById(R.id.price_deal);

        buy = rootView.findViewById(R.id.buy);
        buy.setOnClickListener(this);

        isBuyText = rootView.findViewById(R.id.is_buy);

        if(MyWallet.getInstance() != null && MyWallet.getInstance().getAccountObject().size()>0){
            isBuyText.setText(isBuyText.getText() + ":" +MyWallet.getInstance().getAccountObject().get(0).account.name);
        }


        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == selectCoin){
            String[] items = getActivity().getResources().getStringArray(R.array.assets);
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("select coin")
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectAsset = items[which];

                            FullAccountObject o = MyWallet.getInstance().getAccountObject().get(0);

                            for(FullAccountObject.Balances b :o.balances){
                                Asset asset = (Asset) BtsContorler.getInstance().getDataSync(b.asset_type);
                                if(asset != null && asset.getSymbol().equals("CNY")){
                                    AssetAmount assetAmount = new AssetAmount(b.balance,asset);
                                    baseBalacne.setText("CNY:" + assetAmount.getBalance(asset));
                                    baseAsset = asset;
                                }else if(asset != null && asset.getSymbol().equals(selectAsset)){
                                    AssetAmount assetAmount = new AssetAmount(b.balance,asset);
                                    targetBalacne.setText(selectAsset + ":" + assetAmount.getBalance(asset));
                                    targetAsset = asset;
                                }

                            }
//                            BtsRequest r = BtsRequestHelper.get_limit_orders(baseAsset.getObjectId(),targetAsset.getObjectId(),20,new LimitOrderCallback());
//                            BtsContorler.getInstance().send(r);
                        }
                    }).create();
            dialog.show();
        }else if(v == selectBuy){
            String[] items = new String[]{"buy","sale"};
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("buy?")
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0){
                                isBuy = true;
                                isBuyText.setText("Buy");
                                if(MyWallet.getInstance() != null && MyWallet.getInstance().getAccountObject().size()>0){
                                    isBuyText.setText(isBuyText.getText()  + ":" + MyWallet.getInstance().getAccountObject().get(0).account.name);
                                }
                            }else {
                                isBuy = false;
                                isBuyText.setText("Sale");
                                if(MyWallet.getInstance() != null && MyWallet.getInstance().getAccountObject().size()>0){
                                    isBuyText.setText(isBuyText.getText()  + ":" + MyWallet.getInstance().getAccountObject().get(0).account.name);
                                }
                            }
                        }
                    }).create();
            dialog.show();
        }else if(v == buy){
            String priceText = price.getText().toString();
            if(baseAsset == null || targetAsset == null || baseCount.getText().length() == 0 || priceText.length() == 0 || targetCount.getText().length() == 0){
                return;
            }
            FullAccountObject account = MyWallet.getInstance().getAccountObject().get(0);
            LimitOrderCreateOperation op = null;
            BigDecimal base = new BigDecimal(baseCount.getText().toString());
            BigDecimal target = new BigDecimal(targetCount.getText().toString());
            if(isBuy){
                AssetAmount targetAmount = new AssetAmount(target,targetAsset,true);
                AssetAmount baseAmount = new AssetAmount(base,baseAsset,true);
                int ex = (int)((System.currentTimeMillis() + 60 *60 * 1000)/1000);
                op = new LimitOrderCreateOperation(new UserAccount(account.account.getObjectId()),baseAmount,targetAmount,ex,false);
            }else{
                AssetAmount targetAmount = new AssetAmount(target,targetAsset,true);
                AssetAmount baseAmount = new AssetAmount(base,baseAsset,true);
                int ex = (int)((System.currentTimeMillis() + 60 *60 * 1000)/1000);
                op = new LimitOrderCreateOperation(new UserAccount(account.account.getObjectId()),targetAmount,baseAmount,ex,false);
            }
            dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
            dialog.show();
            BtsRequest r = BtsRequestHelper.get_required_fees(RPC.CALL_DATABASE,new JsonElement[]{op.toJsonObject()},"1.3.113",new FeeCallback(op));
            BtsContorler.getInstance().send(r);
        }

    }

    private class FeeCallback implements BtsRequest.CallBack{

        private final LimitOrderCreateOperation op;

        public FeeCallback(LimitOrderCreateOperation op) {
            this.op = op;
        }

        @Override
        public void onResult(BtsRequest request, JsonElement data) {

            AssetAmount aa = BtsContorler.getInstance().parse(data.getAsJsonArray().get(0),AssetAmount.class);
            op.setFee(aa);


            BtsRequest r = BtsRequestHelper.get_dynamic_global_properties(RPC.CALL_DATABASE,new GlobalPropertiesCallback(op));
            BtsContorler.getInstance().send(r);
        }

        @Override
        public void onError(JRpcError error) {
            error(error);
        }
    }

    private void error(JRpcError error){
        if(dialog != null || dialog.isShowing()){
            dialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void success(JsonElement data){
        if(dialog != null || dialog.isShowing()){
            dialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(),data.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class GlobalPropertiesCallback implements BtsRequest.CallBack{

        private LimitOrderCreateOperation op;

        public GlobalPropertiesCallback(LimitOrderCreateOperation op) {
            this.op = op;
        }

        @Override
        public void onResult(BtsRequest request, JsonElement data) {
             int ex = (int)((System.currentTimeMillis() +  12 *60 *60 * 1000)/1000);
            dynamic_global_property_object d = BtsContorler.getInstance().parse(data,dynamic_global_property_object.class);
            if(MyWallet.getInstance().is_locked()){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final EditText editText = new EditText(getActivity());
                        AlertDialog.Builder inputDialog =
                                new AlertDialog.Builder(getActivity());
                        inputDialog.setTitle("pwd").setView(editText);
                        inputDialog.setPositiveButton("ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String pwd = editText.getText().toString();
                                        MyWallet.getInstance().unlock(pwd);
                                        List<BaseOperation> ll = new ArrayList<>();
                                        ll.add(op);
                                        Transaction transaction = new Transaction(MyWallet.getInstance().getKey(op.getSeller().getObjectId()),new BlockData(d.head_block_number,d.head_block_id,ex),ll);
                                        BtsRequest r = BtsRequestHelper.broadcast_transaction(transaction.toJsonObject(),new TransactionCallback());
                                        BtsContorler.getInstance().send(r);
                                    }
                                }).show();
                    }
                });
            }else{
                List<BaseOperation> ll = new ArrayList<>();
                ll.add(op);
                Transaction transaction = new Transaction(MyWallet.getInstance().getKey(op.getSeller().getObjectId()),new BlockData(d.head_block_number,d.head_block_id,ex),ll);
                BtsRequest r = BtsRequestHelper.verify_authority(transaction.toJsonObject(),new TransactionCallback());
                BtsContorler.getInstance().send(r);
            }
        }

        @Override
        public void onError(JRpcError error) {
            error(error);
        }
    }

    private class TransactionCallback implements BtsRequest.CallBack{

        @Override
        public void onResult(BtsRequest request, JsonElement data) {
            success(data);
        }

        @Override
        public void onError(JRpcError error) {
            error(error);
        }
    }

    private class LimitOrderCallback implements BtsRequest.CallBack{

        @Override
        public void onResult(BtsRequest request, JsonElement data) {
            System.out.println(data.toString());
        }

        @Override
        public void onError(JRpcError error) {

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String priceText = price.getText().toString();
        if(baseAsset == null || targetAsset == null || priceText.length() == 0 || targetCount.getText().length() == 0){
            return;
        }
        BigDecimal p = new BigDecimal(priceText);
        BigDecimal target = new BigDecimal(targetCount.getText().toString());

        String base = target.multiply(p).toString();

        baseCount.setText(base);

    }
}
