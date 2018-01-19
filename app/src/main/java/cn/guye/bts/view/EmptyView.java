package cn.guye.bts.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.guye.bts.R;

public class EmptyView extends FrameLayout {

    private View pg;
    private View icon;
    private TextView title;

    public EmptyView(Context context) {
        super(context);
        inflate(context, R.layout.view_empty,this);
        pg = findViewById(R.id.pb_load);
        icon = findViewById(R.id.item_icon);
        title = findViewById(R.id.item_title);
    }

    public void setLoading(){
        pg.setVisibility(View.VISIBLE);
        icon.setVisibility(View.GONE);
        title.setText(R.string.loading);
    }

    public void setError(String text){
        pg.setVisibility(View.GONE);
        icon.setVisibility(View.VISIBLE);
        title.setText(text);
    }
}
