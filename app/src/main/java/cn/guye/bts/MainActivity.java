package cn.guye.bts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.guye.bitshares.BtsApi;
import cn.guye.bts.contorl.BtsContorler;
import cn.guye.bts.view.CustomDialog;
import cn.guye.tools.jrpclib.JRpc;
import cn.guye.tools.jrpclib.RpcNotice;

public class MainActivity extends AppCompatActivity {

    private Class[] tabClass = {WalletFragment.class,MarketFragment.class, MyAccountFrament.class};
    private CustomDialog dialog ;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int index = item.getOrder();

            currentTabIndex = index;
            switchTab(index);
            return true;
        }
    };
    private int currentTabIndex;
    private Fragment currentFragment;

    private void switchTab(int position){
        currentTabIndex = position;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(currentFragment != null){
            transaction.hide(currentFragment);
        }
        Fragment toFragment = fragmentManager.findFragmentByTag(String.valueOf(position));
        if(toFragment == null){
            toFragment = Fragment.instantiate(this,tabClass[position].getName());
            transaction.add(R.id.main_content,toFragment,String.valueOf(position));
        }else{
            transaction.show(toFragment);
        }
        currentFragment = toFragment;
        if(!MainActivity.this.isFinishing()){
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        switchTab(currentTabIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BtsContorler contorler = BtsContorler.getInstance();
        if(contorler.getStatus() != BtsApi.STATUS_CONNECTED){//TODO more switch
            contorler.start();
            dialog = new CustomDialog(this, R.style.CustomDialog);
            dialog.show();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventBus(BtsContorler.BtsConnectEvent e){

            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }

            if(e.status != BtsApi.STATUS_CONNECTED){
                Toast.makeText(this,e.desc,Toast.LENGTH_SHORT).show();
            }

    }

}
