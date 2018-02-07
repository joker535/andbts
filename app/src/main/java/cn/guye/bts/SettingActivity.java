package cn.guye.bts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;

/**
 * Created by nieyu2 on 18/2/7.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private View logout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v == logout){
            new File(getDataDir(),"stor").delete();
            Toast.makeText(this,"logout ok",Toast.LENGTH_SHORT).show();
        }
    }
}
