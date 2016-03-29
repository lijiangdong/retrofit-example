package com.example.ljd.retrofit;

import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ljd.retrofit.fragment.MainFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private GitHubApi gitHubService;


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new MainFragment(), this.toString())
                    .commit();
        }
        String githubToken = getResources().getString(R.string.github_token);
        gitHubService = GitHubService.createGitHubService(githubToken);
    }

    @Override
    public void onClick(View v) {
        requestGitHubContributors();
    }

    private void requestGitHubContributors(){
        Call<List<Contributor>> call = gitHubService.contributors1("square", "retrofit");
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                Log.e("onResponse是否运行在主线程", (Looper.getMainLooper() == Looper.myLooper()) + "");
                Log.e("message", response.message());
                List<Contributor> list = response.body();
                Log.e("string", response.body().toString());
                for (Contributor contributor : list) {
                    Log.d("login", contributor.login);
                    Log.d("contributions", contributor.contributions + "");
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {
                Log.e("onFailure是否运行在主线程", (Looper.getMainLooper() == Looper.myLooper()) + "");
            }
        });
    }



}
