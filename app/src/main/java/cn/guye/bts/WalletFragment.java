package cn.guye.bts;


import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import cn.guye.bitshares.models.FullAccountObject;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().register(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResult(BtsRequest request, JsonElement data) {
        if(request.getMethod().equals(RPC.CALL_GET_ACCOUNT_BY_NAME)){
            AccountObject my = BtsContorler.getInstance().parse(data,AccountObject.class);

            BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE,new String[]{"zipian1"},false,this);
            BtsContorler.getInstance().send(r);

        }else{
            FullAccountObject fullAccountObject = BtsContorler.getInstance().parse(data.getAsJsonArray().get(0).getAsJsonArray().get(1),FullAccountObject.class);

            System.out.println(fullAccountObject.account.active.getKeyAuthList().get(0).getAddress());

            importfile();

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

        WalletBackup walletBackup = FileBin.deserializeWalletBackup(byteContent, "woaimaomao535");
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
