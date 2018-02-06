package cn.guye.bts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.guye.bitshares.ErrorCode;
import cn.guye.bitshares.models.backup.FileBin;
import cn.guye.bitshares.models.backup.LinkedAccount;
import cn.guye.bitshares.models.backup.PrivateKeyBackup;
import cn.guye.bitshares.models.backup.WalletBackup;
import cn.guye.bts.contorl.MyWallet;

/**
 * Created by nieyu2 on 18/2/6.
 */

public class ImportBinfileFragment extends Fragment implements View.OnClickListener {

    public static final int SELECT_FILE_CODE = 10;
    private View selectFile;
    private EditText pwd;
    private View login;
    private TextView filePath;
    private String file;
    private WalletBackup walletBackup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_import_binfile,container,false);
        selectFile = rootView.findViewById(R.id.select_file);
        pwd = rootView.findViewById(R.id.ed_pwd);
        login = rootView.findViewById(R.id.login);
        filePath = rootView.findViewById(R.id.file_path);
        login.setOnClickListener(this);
        selectFile.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v == selectFile){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, SELECT_FILE_CODE);
        }else{
            importfile();
        }
    }

    public void setFile(String file) {
        this.file = file;
        filePath.setText(file);
    }

    private void importfile(){
        File file = new File(this.file);
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

        walletBackup = FileBin.deserializeWalletBackup(byteContent, pwd.getText().toString());
        if (walletBackup == null) {
            getActivity().setResult(ErrorCode.ERROR_FILE_BIN_PASSWORD_INVALID);
            getActivity().finish();
        }

        String strBrainKey = walletBackup.getWallet(0).decryptBrainKey(pwd.getText().toString());
        LinkedAccount linkedAccount = walletBackup.getLinkedAccounts()[0];
        List<String> list = new ArrayList<>();
        for (LinkedAccount account:walletBackup.getLinkedAccounts()){
            list.add(account.getName());
        }
        List<PrivateKeyBackup> pks = new ArrayList<>();
        for (PrivateKeyBackup pk:walletBackup.getPrivateKeys()){
            pks.add(pk);
        }

        MyWallet myWallet = MyWallet.import_brain_key(strBrainKey,pks,list,pwd.getText().toString());
        MyWallet.setInstance(myWallet);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

}
