package cn.guye.bts;


import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

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
import cn.guye.bitshares.models.Price;
import cn.guye.bitshares.models.backup.FileBin;
import cn.guye.bitshares.models.backup.LinkedAccount;
import cn.guye.bitshares.models.backup.WalletBackup;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.models.chain.dynamic_global_property_object;
import cn.guye.bitshares.models.chain.signed_transaction;

import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bitshares.wallet.AccountObject;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.types;
import cn.guye.bts.contorl.MyWallet;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class WalletFragment extends BaseFragment implements BtsRequest.CallBack {


    private TextView textView;
    private WalletBackup walletBackup;
    private FullAccountObject[] fullAccountObject;
    private Operations.LimitOrderCreateOperation o;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().register(this);
        textView = new TextView(getActivity());
        return textView;
    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
        if(request.getMethod().equals(RPC.CALL_GET_ACCOUNT_BY_NAME)){
            AccountObject my = BtsContorler.getInstance().parse(data,AccountObject.class);

            BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE,new String[]{"guye535","zipian3"},false,this);
            BtsContorler.getInstance().send(r);

        }else if(request.getMethod().equals(RPC.CALL_GET_FULL_ACCOUNTS)){
            JsonArray array = data.getAsJsonArray();

            fullAccountObject = new FullAccountObject[array.size()];
            final StringBuilder sb = new StringBuilder();
            for (int i  = 0 ; i < fullAccountObject.length ; i++){
                fullAccountObject[i] = BtsContorler.getInstance().parse(array.get(i).getAsJsonArray().get(1),FullAccountObject.class);
                sb.append("======================\n");
                sb.append(fullAccountObject[i].account.name).append("\n");
                for (FullAccountObject.Balances b :
                        fullAccountObject[i].balances) {
                    Asset base  = (Asset) BtsContorler.getInstance().getDataSync(b.asset_type);
                    String bName = base== null?b.asset_type:base.getSymbol();
                    String bl = base==null?b.balance.toString():Price.get_asset_amount(b.balance,base).toString();
                    sb.append(bName).append(" : ").append(bl).append("\n");
                }

                sb.append("order:\n");
                for (LimitOrder lo:
                     fullAccountObject[i].limit_orders) {
                    Price p = lo.getSellPrice();
                    Asset base  = (Asset) BtsContorler.getInstance().getDataSync(p.base.getAsset().getObjectId());
                    Asset quote  = (Asset) BtsContorler.getInstance().getDataSync(p.quote.getAsset().getObjectId());
                    String bName = base== null?p.base.getAsset().getObjectId():base.getSymbol();
                    String qName = quote== null?p.quote.getAsset().getObjectId():quote.getSymbol();
                    String amount = base==null?"-":Price.get_asset_amount(p.base.getAmount(),base).toString();
                    sb.append(bName).append("/").append(qName).append(" : ").append(lo.getSellPrice().base2Quote(base,quote)).append(" for sell:").append(amount).append("\n");
                }
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(sb.toString());
                }
            });

            o = new Operations.LimitOrderCreateOperation();
            o.amount_to_sell = new AssetAmount(new BigDecimal(10000000),new GrapheneObject("1.3.0"));
            o.expiration = new Date(System.currentTimeMillis() + 365 * 24 * 60 *60 * 1000);
            o.fee = new AssetAmount(new BigDecimal(0),new GrapheneObject("1.3.0"));
            o.min_to_receive = new AssetAmount(new BigDecimal(100000000000000L),new GrapheneObject("1.3.131"));
            o.seller = fullAccountObject[1].account.getObjectId();
            o.fill_or_kill = false;


            BtsRequest r = BtsRequestHelper.get_required_fees(RPC.CALL_DATABASE,new Operations[]{o},"1.3.0",this);
            BtsContorler.getInstance().send(r);

            importfile();

        }else if(request.getMethod().equals(RPC.CALL_GET_REQUIRED_FEES)){
            AssetAmount aa = BtsContorler.getInstance().parse(data.getAsJsonArray().get(0),AssetAmount.class);
            o.fee = aa;

            BtsRequest r = BtsRequestHelper.get_dynamic_global_properties(RPC.CALL_DATABASE,this);
            BtsContorler.getInstance().send(r);

        }else if(request.getMethod().equals(RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES)){

            dynamic_global_property_object d = BtsContorler.getInstance().parse(data,dynamic_global_property_object.class);
            MyWallet my = new MyWallet();

            signed_transaction signed_transaction = new signed_transaction();
            signed_transaction.operations.add(o);
            signed_transaction.expiration = o.expiration;


            String bk = walletBackup.getWallet(0).decryptBrainKey("woaimaomao535");
            my.import_brain_key(fullAccountObject[1].account.name,bk,fullAccountObject[1].account);

            my.sign_transaction(signed_transaction,walletBackup,d,fullAccountObject[1].account);

            BtsRequest r = BtsRequestHelper.verify_authority(RPC.CALL_DATABASE,signed_transaction,this);
            BtsContorler.getInstance().send(r);

        }else{
            System.out.println(data.toString());
        }
    }

    @Override
    public void onError(JRpcError error) {

    }

    public void importfile(){
        File file = new File(Environment.getExternalStorageDirectory(),"/bts_test_20180125.bin");
        if (file.exists() == false) {
            return;
        }

        int nSize = (int)file.length();

        final byte[] byteContent = new byte[nSize];

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteContent, 0, byteContent.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ;
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }

        walletBackup = FileBin.deserializeWalletBackup(byteContent, "woaimaomao535");
        if (walletBackup == null) {
            return ;
        }

        String strBrainKey = walletBackup.getWallet(0).decryptBrainKey("woaimaomao535");
        //LinkedAccount linkedAccount = walletBackup.getLinkedAccounts()[0];

        int nRet = ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
        for (LinkedAccount linkedAccount : walletBackup.getLinkedAccounts()) {
            nRet = import_brain_key(linkedAccount.getName(), "woaimaomao535", strBrainKey);
            if (nRet == 0) {
                break;
            }
        }

    }

    public int import_brain_key(String strAccountNameOrId,
                                String strPassword,
                                String strBrainKey) {


        return 0;
    }


    @Subscribe
    public void onBtsEvent(BtsContorler.BtsConnectEvent e){
        if(e.status == BtsApi.STATUS_CONNECTED){
            BtsRequest btsRequest = BtsRequestHelper.get_account_by_name(RPC.CALL_DATABASE , "joker53535",this);
            BtsContorler.getInstance().send(btsRequest);
        }
    }
}
