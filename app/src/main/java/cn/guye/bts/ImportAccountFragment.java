package cn.guye.bts;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cn.guye.bts.contorl.MyWallet;

/**
 * Created by nieyu2 on 18/2/6.
 */

public class ImportAccountFragment extends Fragment implements View.OnClickListener {

    private EditText user;
    private EditText pwd;
    private View login;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_import_account,container,false);
        user = rootView.findViewById(R.id.ed_user);
        pwd = rootView.findViewById(R.id.ed_pwd);
        login = rootView.findViewById(R.id.login);

        login.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == login){
            MyWallet wallet = MyWallet.import_account_password(user.getText().toString(),pwd.getText().toString());
            MyWallet.setInstance(wallet);
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }
}
