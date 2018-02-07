package cn.guye.bts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.guye.bitshares.BtsApi;
import cn.guye.bitshares.RPC;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.contorl.BtsRequest;
import cn.guye.bts.contorl.BtsRequestHelper;
import cn.guye.bts.contorl.MyWallet;
import cn.guye.bts.view.CustomDialog;
import cn.guye.tools.jrpclib.JRpc;
import cn.guye.tools.jrpclib.JRpcError;
import cn.guye.tools.jrpclib.RpcNotice;

public class MainActivity extends AppCompatActivity {

    private Class[] tabClass = {WalletFragment.class, MarketFragment.class, MyAccountFrament.class};
    private CustomDialog dialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {


            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    currentTabIndex = 0;
                    break;
                case R.id.navigation_market:
                    currentTabIndex = 1;
                    break;
                case R.id.navigation_myaccount:
                    currentTabIndex = 2;
                    break;
            }
            switchTab(currentTabIndex);
            return true;
        }
    };
    private int currentTabIndex;
    private Fragment currentFragment;

    private void switchTab(int position) {
        currentTabIndex = position;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        Fragment toFragment = fragmentManager.findFragmentByTag(String.valueOf(position));
        if (toFragment == null) {
            toFragment = Fragment.instantiate(this, tabClass[position].getName());
            transaction.add(R.id.main_content, toFragment, String.valueOf(position));
        } else {
            transaction.show(toFragment);
        }
        currentFragment = toFragment;
        if (!MainActivity.this.isFinishing()) {
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            initAccount();
        }else{
            Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
        }

    }

    private void initAccount() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BtsContorler contorler = BtsContorler.getInstance();
                if (contorler.getStatus() != BtsApi.STATUS_CONNECTED) {//TODO more switch
                    contorler.start();
                    dialog = new CustomDialog(MainActivity.this, R.style.CustomDialog);
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        switchTab(currentTabIndex);

        checkPermission();
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(this,SettingActivity.class);
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyWallet wallet = MyWallet.load();
        if (wallet == null) {
            Intent i = new Intent(this, ImportActivty.class);
            startActivityForResult(i,1);
        }else{
            MyWallet.setInstance(wallet);
            initAccount();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventBus(BtsContorler.BtsConnectEvent e) {

        List<String> account = MyWallet.getInstance().getAccounts();
        BtsRequest r = BtsRequestHelper.get_full_accounts(RPC.CALL_DATABASE, account.toArray(new String[]{}), false, new BtsRequest.CallBack() {
            @Override
            public void onResult(BtsRequest request, JsonElement data) {
                JsonArray array = data.getAsJsonArray();

                List<FullAccountObject> fullAccountObject = new ArrayList<>();
                final StringBuilder sb = new StringBuilder();
                for (int i  = 0 ; i < array.size() ; i++) {
                    FullAccountObject f = BtsContorler.getInstance().parse(array.get(i).getAsJsonArray().get(1), FullAccountObject.class);
                    fullAccountObject.add(f);
                }
                MyWallet.getInstance().setAccountObject(fullAccountObject);
                EventBus.getDefault().post(MyWallet.getInstance());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        if (e.status != BtsApi.STATUS_CONNECTED) {
                            Toast.makeText(MainActivity.this, e.desc, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(JRpcError error) {

            }
        });
        BtsContorler.getInstance().send(r);
    }



}
