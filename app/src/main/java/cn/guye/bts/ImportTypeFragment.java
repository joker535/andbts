package cn.guye.bts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nieyu2 on 18/2/6.
 */

public class ImportTypeFragment extends Fragment implements View.OnClickListener {
    private View accountType;
    private View binfileType;
    private View brainkeyType;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_import_type,container,false);
        accountType = rootView.findViewById(R.id.account_type);
        binfileType = rootView.findViewById(R.id.binfile_type);
        brainkeyType = rootView.findViewById(R.id.brainkey_type);

        accountType.setOnClickListener(this);
        binfileType.setOnClickListener(this);
        brainkeyType.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == accountType){
            ((ImportActivty)getActivity()).switchTab(1);
        }else if(v == binfileType){
            ((ImportActivty)getActivity()).switchTab(2);
        }
    }
}
