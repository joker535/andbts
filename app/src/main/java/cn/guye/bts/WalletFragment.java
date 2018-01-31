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

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.ErrorCode;
import cn.guye.bitshares.RPC;
import cn.guye.bitshares.errors.MalformedAddressException;
import cn.guye.bitshares.models.Address;
import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bitshares.models.LimitOrder;
import cn.guye.bitshares.models.Price;
import cn.guye.bitshares.models.backup.FileBin;
import cn.guye.bitshares.models.backup.LinkedAccount;
import cn.guye.bitshares.models.backup.WalletBackup;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bitshares.wallet.AccountObject;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.types;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class WalletFragment extends BaseFragment implements BtsRequest.CallBack {


    private TextView textView;

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

            BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE,new String[]{"guye535","joker53535"},false,this);
            BtsContorler.getInstance().send(r);

        }else{
            JsonArray array = data.getAsJsonArray();

            FullAccountObject[] fullAccountObject = new FullAccountObject[array.size()];
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

//            importfile();

        }
    }

    @Override
    public void onError(JRpcError error) {

    }

//    public void importfile(){
//        File file = new File(Environment.getExternalStorageDirectory(),"/bts_test_20180125.bin");
//        if (file.exists() == false) {
//            return;
//        }
//
//        int nSize = (int)file.length();
//
//        final byte[] byteContent = new byte[nSize];
//
//        FileInputStream fileInputStream;
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(byteContent, 0, byteContent.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return ;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ;
//        }
//
//        WalletBackup walletBackup = FileBin.deserializeWalletBackup(byteContent, "woaimaomao535");
//        if (walletBackup == null) {
//            return ;
//        }
//
//        String strBrainKey = walletBackup.getWallet(0).decryptBrainKey("woaimaomao535");
//        //LinkedAccount linkedAccount = walletBackup.getLinkedAccounts()[0];
//
//        int nRet = ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
//        for (LinkedAccount linkedAccount : walletBackup.getLinkedAccounts()) {
//            nRet = import_brain_key(linkedAccount.getName(), "woaimaomao535", strBrainKey);
//            if (nRet == 0) {
//                break;
//            }
//        }
//
//    }

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
