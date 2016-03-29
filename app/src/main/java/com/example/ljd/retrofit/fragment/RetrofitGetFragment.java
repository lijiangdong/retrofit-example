package com.example.ljd.retrofit.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ljd.retrofit.pojo.Contributor;
import com.example.ljd.retrofit.GitHubApi;
import com.example.ljd.retrofit.GitHubService;
import com.example.ljd.retrofit.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static java.lang.String.format;


public class RetrofitGetFragment extends Fragment {

    @Bind(R.id.retrofit_get_contributors_username)
    EditText mUsername;
    @Bind(R.id.retrofit_get_contributors_repository)
    EditText mRepo;
    @Bind(R.id.retrofit_get_log_list)
    ListView mResultList;

    private ArrayAdapter<String> mAdapter;
    private GitHubApi gitHubService;

    public RetrofitGetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String gitHubToken = getResources().getString(R.string.github_token);
        gitHubService = GitHubService.createGitHubService(gitHubToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.fragment_retrofit_get, container, false);
        ButterKnife.bind(this,layout);
        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<String>());
        mResultList.setAdapter(mAdapter);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_retrofit_get_contributors)
    public void requestGitHubContributors(){
        Call<List<Contributor>> call = gitHubService.contributorsByGetCall(mUsername.getText().toString(), mRepo.getText().toString());
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                List<Contributor> list = response.body();
                for (Contributor c : list) {
                    mAdapter.add(format("%s has made %d contributions to %s",
                            c.getLogin(),
                            c.getContributions(),
                            mRepo.getText().toString()));

                    Timber.d("%s has made %d contributions to %s",
                            c.getLogin(),
                            c.getContributions(),
                            mRepo.getText().toString());
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.btn_retrofit_get_clear)
    public void clearList(){
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

}
