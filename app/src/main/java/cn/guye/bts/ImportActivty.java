package cn.guye.bts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import cn.guye.bts.utils.ConvertUriToFilePath;

public class ImportActivty extends AppCompatActivity {

    public static final int ACCOUNT_MODEL = 1;
    public static final int WALLET_MODEL_WIF_KEY = 2;
    public static final int WALLET_MODEL_BIN_FILE = 3;
    public static final int WALLET_MODEL_BRAIN_KEY = 4;

    private static final int SELECT_FILE_CODE = 1;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private ImportTypeFragment importTypeFragment;
    private ImportAccountFragment accountFragment;
    private ImportBinfileFragment binfileFragment;

    private Class[] tabClass = {ImportTypeFragment.class, ImportAccountFragment.class, ImportBinfileFragment.class};
    private int currentTabIndex;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        switchTab(0);
    }

    public void switchTab(int position) {
        currentTabIndex = position;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        Fragment toFragment = fragmentManager.findFragmentByTag(String.valueOf(position));
        if (toFragment == null) {
            toFragment = Fragment.instantiate(this, tabClass[position].getName());
            if(toFragment instanceof ImportBinfileFragment){
                binfileFragment = (ImportBinfileFragment) toFragment;
            }
            transaction.add(R.id.main_content, toFragment, String.valueOf(position));
        } else {
            transaction.show(toFragment);
        }
        currentFragment = toFragment;
        if (!this.isFinishing()) {
            transaction.commitAllowingStateLoss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String strFilePath = uri.getPath();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String strFilePathAboveKitKat = null;
                try {
                    strFilePathAboveKitKat = ConvertUriToFilePath.getPathFromURI(this, uri);
                    binfileFragment.setFile(strFilePathAboveKitKat);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
