package com.example.ljd.retrofit.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ljd.retrofit.R;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this,layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.btn_retrofit_rxjava,R.id.btn_retrofit_get})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_retrofit_rxjava:
                clickedOn(new RxJavaRetrofitFragment());
                break;
            case R.id.btn_retrofit_get:
                clickedOn(new RetrofitGetFragment());
                break;
        }
    }
    private void clickedOn(@NonNull Fragment fragment) {
        final String tag = fragment.getClass().toString();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }

}
