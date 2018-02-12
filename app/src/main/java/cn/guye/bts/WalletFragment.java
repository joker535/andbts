package cn.guye.bts;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.ErrorCode;
import cn.guye.bitshares.RPC;
import cn.guye.bitshares.errors.MalformedAddressException;
import cn.guye.bitshares.models.Address;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bitshares.models.OperationHistory;
import cn.guye.bitshares.models.Price;
import cn.guye.bitshares.models.Transaction;
import cn.guye.bitshares.models.UserAccount;
import cn.guye.bitshares.models.backup.FileBin;
import cn.guye.bitshares.models.backup.LinkedAccount;
import cn.guye.bitshares.models.backup.Wallet;
import cn.guye.bitshares.models.backup.WalletBackup;
import cn.guye.bitshares.models.chain.BlockData;
import cn.guye.bitshares.models.chain.dynamic_global_property_object;

import cn.guye.bitshares.operations.BaseOperation;
import cn.guye.bitshares.operations.LimitOrderCancelOperation;
import cn.guye.bitshares.operations.LimitOrderCreateOperation;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bitshares.wallet.AccountObject;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.types;
import cn.guye.bts.contorl.MyWallet;
import cn.guye.tools.jrpclib.JRpcError;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class WalletFragment extends BaseFragment implements BtsRequest.CallBack, View.OnClickListener {


    private ListView listview;
    private View update;
    private View myhistory;
    private MyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().register(this);
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        listview = rootView.findViewById(R.id.list_view);
        adapter = new MyAdapter();
        listview.setAdapter(adapter);
        update = rootView.findViewById(R.id.update);
        myhistory = rootView.findViewById(R.id.myhistory);
        update.setOnClickListener(this);
        myhistory.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
       if (request.getMethod().equals(RPC.CALL_GET_FULL_ACCOUNTS)) {
//            JsonArray array = data.getAsJsonArray();
//
//            fullAccountObject = new FullAccountObject[array.size()];
//            final StringBuilder sb = new StringBuilder();
//            for (int i  = 0 ; i < fullAccountObject.length ; i++){
//                fullAccountObject[i] = BtsContorler.getInstance().parse(array.get(i).getAsJsonArray().get(1),FullAccountObject.class);
//                sb.append("======================\n");
//                sb.append(fullAccountObject[i].account.name).append("\n");
//                for (FullAccountObject.Balances b :
//                        fullAccountObject[i].balances) {
//                    Asset base  = (Asset) BtsContorler.getInstance().getDataSync(b.asset_type);
//                    String bName = base== null?b.asset_type:base.getSymbol();
//                    String bl = base==null?b.balance.toString():Price.get_asset_amount(b.balance,base).toString();
//                    sb.append(bName).append(" : ").append(bl).append("\n");
//                }
//
//                sb.append("order:\n");
//                for (LimitOrder lo:
//                     fullAccountObject[i].limit_orders) {
//                    Price p = lo.getSellPrice();
//                    Asset base  = (Asset) BtsContorler.getInstance().getDataSync(p.base.getAsset().getObjectId());
//                    Asset quote  = (Asset) BtsContorler.getInstance().getDataSync(p.quote.getAsset().getObjectId());
//                    String bName = base== null?p.base.getAsset().getObjectId():base.getSymbol();
//                    String qName = quote== null?p.quote.getAsset().getObjectId():quote.getSymbol();
//                    String amount = base==null?"-":Price.get_asset_amount(p.base.getAmount(),base).toString();
//                    sb.append(bName).append("/").append(qName).append(" : ").append(lo.getSellPrice().base2Quote(base,quote)).append(" for sell:").append(amount).append("\n");
//                }
//            }
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    textView.setText(sb.toString());
//                }
//            });
//
//            o = new Operations.LimitOrderCreateOperation();
//            o.amount_to_sell = new AssetAmount(new BigDecimal(10000000),new GrapheneObject("1.3.0"));
//            o.expiration = new Date(System.currentTimeMillis() + 2* 24 * 60 *60 * 1000);
//            o.fee = new AssetAmount(new BigDecimal(0),new GrapheneObject("1.3.0"));
//            o.min_to_receive = new AssetAmount(new BigDecimal(100000000000000L),new GrapheneObject("1.3.131"));
//            o.seller = fullAccountObject[1].account.getObjectId();
//            o.fill_or_kill = false;
//            ee = (int)((System.currentTimeMillis() + 60 *60 * 1000)/1000);
//
//            op = new LimitOrderCreateOperation(new UserAccount(fullAccountObject[1].account.getObjectId()),o.amount_to_sell,o.min_to_receive,ee,false);
//
//
//            BtsRequest r = BtsRequestHelper.get_required_fees(RPC.CALL_DATABASE,new Operations[]{o},"1.3.0",this);
//            BtsContorler.getInstance().send(r);


        } else if (request.getMethod().equals(RPC.CALL_GET_REQUIRED_FEES)) {
//            AssetAmount aa = BtsContorler.getInstance().parse(data.getAsJsonArray().get(0),AssetAmount.class);
//            o.fee = aa;
//            op.setFee(aa);
//            BtsRequest r = BtsRequestHelper.get_dynamic_global_properties(RPC.CALL_DATABASE,this);
//            BtsContorler.getInstance().send(r);

        } else if (request.getMethod().equals(RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES)) {
//            System.out.println(data.toString());
//            dynamic_global_property_object d = BtsContorler.getInstance().parse(data,dynamic_global_property_object.class);
//            MyWallet my = new MyWallet();
//
//            signed_transaction signed_transaction = new signed_transaction();
//            signed_transaction.operations.add(o);
//
//            String bk = walletBackup.getWallet(0).decryptBrainKey("woaimaomao535");
//            PrivateKey pp = my.import_brain_key(fullAccountObject[1].account.name,bk,fullAccountObject[1].account);
//
//            my.sign_transaction(signed_transaction,walletBackup,d,fullAccountObject[1].account);
//            List<BaseOperation> ll = new ArrayList<>();
//            ll.add(op);
//            Transaction transaction = new Transaction(pp.getEC(),new BlockData(d.head_block_number,d.head_block_id,ee),ll);
//
//            BtsRequest r = BtsRequestHelper.verify_authority(RPC.CALL_DATABASE,transaction.toJsonObject(),this);
//            BtsContorler.getInstance().send(r);

        } else {
            System.out.println(data.toString());
        }
    }

    @Override
    public void onError(JRpcError error) {

    }


    @Subscribe
    public void onBtsEvent(MyWallet e) {
//        final StringBuilder sb = new StringBuilder();
        List<FullAccountObject> as = e.getAccountObject();
//        for (int i = 0; i < as.size(); i++) {
//            sb.append("======================\n");
//            sb.append(as.get(i).account.name).append("\n");
//            for (FullAccountObject.Balances b :
//                    as.get(i).balances) {
//                Asset base = (Asset) BtsContorler.getInstance().getDataSync(b.asset_type);
//                String bName = base == null ? b.asset_type : base.getSymbol();
//                String bl = base == null ? b.balance.toString() : Price.get_asset_amount(b.balance, base).toString();
//                sb.append(bName).append(" : ").append(bl).append("\n");
//            }
//
//            sb.append("order:\n");
//            for (LimitOrder lo :
//                    as.get(i).limit_orders) {
//                Price p = lo.getSellPrice();
//                Asset base = (Asset) BtsContorler.getInstance().getDataSync(p.base.getAsset().getObjectId());
//                Asset quote = (Asset) BtsContorler.getInstance().getDataSync(p.quote.getAsset().getObjectId());
//                String bName = base == null ? p.base.getAsset().getObjectId() : base.getSymbol();
//                String qName = quote == null ? p.quote.getAsset().getObjectId() : quote.getSymbol();
//                String amount = base == null ? "-" : Price.get_asset_amount(p.base.getAmount(), base).toString();
//                sb.append(bName).append("/").append(qName).append(" : ").append(lo.getSellPrice().base2Quote(base, quote)).append(" for sell:").append(amount).append("\n");
//            }
//        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.list = as;
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == update) {
            List<String> account = MyWallet.getInstance().getAccounts();
//            SharedPreferences sp = getActivity().getSharedPreferences("bts",MODE_PRIVATE);
//            String accounts = sp.getString("fav","");
//            String[] as = accounts.split(",");
//            for (String a:
//                    as) {
//                if(!account.contains(a)){
//                    account.add(a.trim());
//                }
//            }
            BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE, account.toArray(new String[]{}), false, new BtsRequest.CallBack() {
                @Override
                public void onResult(BtsRequest request, JsonElement data) {
                    JsonArray array = data.getAsJsonArray();

                    List<FullAccountObject> fullAccountObject = new ArrayList<>();
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < array.size(); i++) {
                        FullAccountObject f = BtsContorler.getInstance().parse(array.get(i).getAsJsonArray().get(1), FullAccountObject.class);
                        fullAccountObject.add(f);
                    }
                    MyWallet.getInstance().setAccountObject(fullAccountObject);
                    onBtsEvent(MyWallet.getInstance());

                }

                @Override
                public void onError(JRpcError error) {

                }
            });
            BtsContorler.getInstance().send(r);
        } else if (v == myhistory) {
            Intent i = new Intent(getActivity(), AccountHistoryActivity.class);
            i.putExtra("id", MyWallet.getInstance().getAccountObject().get(0).account.getObjectId());
//            i.putExtra("id","1.2.495937");
            startActivity(i);

        }else{
            if(v instanceof TextView){
                if(v.getTag() instanceof LimitOrder){
                    LimitOrder l = (LimitOrder) v.getTag();

                    showNormalDialog(l);
                }
            }
        }
    }

    private void showNormalDialog(final LimitOrder l){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setTitle("cancel order?");
        normalDialog.setMessage("?");
        normalDialog.setPositiveButton("ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LimitOrderCancelOperation lop = new LimitOrderCancelOperation(l,new UserAccount(l.getSeller()));

                        BtsRequest r = BtsRequestHelper.get_dynamic_global_properties(RPC.CALL_DATABASE, new BtsRequest.CallBack() {
                            public long ee;

                            @Override
                            public void onResult(BtsRequest request, JsonElement data) {
                                dynamic_global_property_object d = BtsContorler.getInstance().parse(data,dynamic_global_property_object.class);
                                MyWallet myWallet = MyWallet.getInstance();
                                ee = (int)((System.currentTimeMillis() + 24 * 60 *60 * 1000)/1000);
                                List<BaseOperation> ll = new ArrayList<>();
                                ll.add(lop);

                                boolean hasKey = myWallet.hasAccount(l.getSeller());
                                if(myWallet.is_locked()){
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final EditText editText = new EditText(getActivity());
                                            AlertDialog.Builder inputDialog =
                                                    new AlertDialog.Builder(getActivity());
                                            inputDialog.setTitle("我是一个输入Dialog").setView(editText);
                                            inputDialog.setPositiveButton("ok",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            String pwd = editText.getText().toString();
                                                            myWallet.unlock(pwd);
                                                            Transaction transaction = new Transaction(myWallet.getKey(l.getSeller()),new BlockData(d.head_block_number,d.head_block_id,ee),ll);
                                                            List<AssetAmount> fee = new ArrayList<>();
                                                            fee.add(new AssetAmount(new BigDecimal(9),new GrapheneObject("1.3.0")));
                                                            transaction.setFees(fee);

                                                            BtsRequest r = BtsRequestHelper.broadcast_transaction(transaction.toJsonObject(),new BtsRequest.CallBack() {
                                                                @Override
                                                                public void onResult(BtsRequest request, JsonElement data) {

                                                                }

                                                                @Override
                                                                public void onError(JRpcError error) {

                                                                }
                                                            });
                                                            BtsContorler.getInstance().send(r);
                                                        }
                                                    }).show();
                                        }
                                    });
                                }else{
                                    Transaction transaction = new Transaction(myWallet.getKey(l.getSeller()),new BlockData(d.head_block_number,d.head_block_id,ee),ll);
                                    List<AssetAmount> fee = new ArrayList<>();
                                    fee.add(new AssetAmount(new BigDecimal(9),new GrapheneObject("1.3.0")));
                                    transaction.setFees(fee);

                                    BtsRequest r = BtsRequestHelper.verify_authority( transaction.toJsonObject(), new BtsRequest.CallBack() {
                                        @Override
                                        public void onResult(BtsRequest request, JsonElement data) {
                                            Toast.makeText(getActivity(), data.toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(JRpcError error) {

                                        }
                                    });
                                    BtsContorler.getInstance().send(r);
                                }
                            }

                            @Override
                            public void onError(JRpcError error) {

                            }
                        });
                        BtsContorler.getInstance().send(r);
                    }
                });
        normalDialog.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // 显示
        normalDialog.show();
    }


    private class MyAdapter extends BaseAdapter {

        private List<FullAccountObject> list = Collections.EMPTY_LIST;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccountView accountView = new AccountView(getActivity());
            accountView.update(list.get(position));

            return accountView;
        }
    }

    private class AccountView extends LinearLayout {

        private FullAccountObject fullAccountObject;

        private TextView name;
        private TextView balances;
        private TextView[] orders;

        public AccountView(Context context) {
            super(context);
            init();
        }

        private void init() {
            setOrientation(LinearLayout.VERTICAL);

        }

        private void update(FullAccountObject a) {
            removeAllViews();
            if (name == null) {
                name = new TextView(getActivity());
                name.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                balances = new TextView(getActivity());
                balances.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            addView(name);
            addView(balances);
            name.setText("$ " + a.account.name + "\nbalances:");
            StringBuilder sb = new StringBuilder();
            for (FullAccountObject.Balances b :
                    a.balances) {
                Asset base = (Asset) BtsContorler.getInstance().getDataSync(b.asset_type);
                String bName = base == null ? b.asset_type : base.getSymbol();
                String bl = base == null ? b.balance.toString() : Price.get_asset_amount(b.balance, base).toString();

                sb.append(bName).append(" : ").append(bl).append("\n");
            }
            balances.setText(sb.toString() + "\norders:");
            orders = new TextView[a.limit_orders.length];
            for (int i = 0; i < orders.length; i++) {
                orders[i] = new TextView(getActivity());
                Price p = a.limit_orders[i].getSellPrice();
                Asset base = (Asset) BtsContorler.getInstance().getDataSync(p.base.getAsset().getObjectId());
                Asset quote = (Asset) BtsContorler.getInstance().getDataSync(p.quote.getAsset().getObjectId());
                String bName = base == null ? p.base.getAsset().getObjectId() : base.getSymbol();
                String qName = quote == null ? p.quote.getAsset().getObjectId() : quote.getSymbol();
                String amount = base == null ? "-" : Price.get_asset_amount(p.base.getAmount(), base).toString();
                sb = new StringBuilder();
                sb.append(bName).append("/").append(qName).append(" : ").append(a.limit_orders[i].getSellPrice().base2Quote(base, quote)).append(" for sell:").append(amount).append("\n");
                orders[i].setText(sb.toString());
                orders[i].setTag(a.limit_orders[i]);
                orders[i].setOnClickListener(WalletFragment.this);
                addView(orders[i]);
            }
        }
    }
}
