package cn.guye.bts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.AssetAmount;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.Transaction;
import cn.guye.bitshares.models.chain.BlockData;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.tools.jrpclib.JRpcError;

/**
 * Created by nieyu2 on 18/2/7.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private View logout;
    private View addFav;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        logout = findViewById(R.id.logout);
        addFav = findViewById(R.id.add_fav);
        logout.setOnClickListener(this);
        addFav.setOnClickListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v == logout){
            new File(getDataDir(),"stor").delete();
            Toast.makeText(this,"logout ok",Toast.LENGTH_SHORT).show();
        }else if(v == addFav){
            final EditText editText = new EditText(this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(this);
            inputDialog.setTitle("Account name or id").setView(editText);
            inputDialog.setPositiveButton("ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sp = getSharedPreferences("bts",MODE_PRIVATE);
                            String accounts = sp.getString("fav","");
                            if(accounts.length() == 0){
                                accounts = editText.getText().toString();
                            }else{
                                accounts = editText.getText().toString() +","+ accounts;
                            }
                            sp.edit().putString("fav",accounts).commit();
                        }
                    }).show();
        }
    }
}
