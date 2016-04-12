package com.example.ljd.retrofit;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.ljd.retrofit.download.DownloadActivity;
import com.example.ljd.retrofit.pojo.Contributor;
import com.example.ljd.retrofit.pojo.Item;
import com.example.ljd.retrofit.pojo.Owner;
import com.example.ljd.retrofit.pojo.RetrofitBean;
import com.example.ljd.retrofit.pojo.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.text.TextUtils.isEmpty;


public class MainActivity extends FragmentActivity{

    private GitHubApi mGitHubService;
    private String mUserName;
    private String mRepo;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private final static String TAG = "MainActivity";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    @Override
    protected void onDestroy() {
        RxUtils.unSubscribeIfNotNull(mSubscriptions);
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    private void initData(){
        ButterKnife.bind(this);
        mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
        mSubscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(mSubscriptions);
        mUserName = getResources().getString(R.string.user_name);
        mRepo = getResources().getString(R.string.repo);
    }

    @OnClick({R.id.btn_retrofit_simple_contributors,
            R.id.btn_retrofit_converter_contributors,
            R.id.btn_retrofit_sync_contributors,
            R.id.btn_add_okhttp_log_contributors,
            R.id.btn_add_header_contributors,
            R.id.btn_retrofit_get_query,
            R.id.btn_retrofit_get_query_map,
            R.id.btn_rxJava_retrofit_contributors,
            R.id.btn_rxJava_retrofit_contributors_with_user_info,
            R.id.btn_download_retrofit,
    })
    public void onClickButton(View v){
        Map<String,String> queryMap = new HashMap<>();
        queryMap.put("q", "retrofit");
        queryMap.put("since","2016-03-29");
        queryMap.put("page","1");
        queryMap.put("per_page", "3");
        switch (v.getId()){
            //简单演示retrofit的使用
            case R.id.btn_retrofit_simple_contributors:
                requestGitHubContributorsSimple();
                break;
            //添加转换器
            case R.id.btn_retrofit_converter_contributors:
                requestGitHubContributorsByConverter();
                break;
            //添加okHttp的Log信息
            case R.id.btn_add_okhttp_log_contributors:
                requestGitHubContributorsAddOkHttpLog();
                break;
            //添加请求头
            case R.id.btn_add_header_contributors:
                requestGitHubContributorsAddHeader();
                break;
            //同步请求
            case R.id.btn_retrofit_sync_contributors:
                requestGitHubContributorsBySync();
                break;
            //通过get请求，使用@Query
            case R.id.btn_retrofit_get_query:
                requestQueryRetrofitByGet(null);
                break;
            //通过get请求，使用@QueryMap
            case R.id.btn_retrofit_get_query_map:
                requestQueryRetrofitByGet(queryMap);
                break;
            //rxJava+retrofit
            case R.id.btn_rxJava_retrofit_contributors:
                requestGitHubContributorsByRxJava();
                break;
            //rxJava+retrofit
            case R.id.btn_rxJava_retrofit_contributors_with_user_info:
                requestGitHubContributorsWithFullUserInfo();
                break;
            //文件下载
            case R.id.btn_download_retrofit:
                Intent intent = new Intent(this, DownloadActivity.class);
                startActivity(intent);
                break;

        }
    }

    /**
     * 简单示例
     */
    private void requestGitHubContributorsSimple(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .build();
        GitHubApi repo = retrofit.create(GitHubApi.class);

        Call<ResponseBody> call = repo.contributorsBySimpleGetCall(mUserName, mRepo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Gson gson = new Gson();
                    ArrayList<Contributor> contributorsList = gson.fromJson(response.body().string(), new TypeToken<List<Contributor>>() {
                    }.getType());
                    for (Contributor contributor : contributorsList) {
                        Log.d("login", contributor.getLogin());
                        Log.d("contributions", contributor.getContributions() + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 转换器
     */
    private void requestGitHubContributorsByConverter(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubApi repo = retrofit.create(GitHubApi.class);
        Call<List<Contributor>> call = repo.contributorsByAddConverterGetCall(mUserName, mRepo);
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                List<Contributor> contributorList = response.body();
                for (Contributor contributor : contributorList){
                    Log.d("login", contributor.getLogin());
                    Log.d("contributions", contributor.getContributions() + "");
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {

            }
        });
    }

    /**
     * 添加日志信息
     */
    private void requestGitHubContributorsAddOkHttpLog(){

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubApi repo = retrofit.create(GitHubApi.class);

        Call<List<Contributor>> call = repo.contributorsByAddConverterGetCall(mUserName, mRepo);
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                List<Contributor> contributorList = response.body();
                for (Contributor contributor : contributorList){
                    Log.d("login", contributor.getLogin());
                    Log.d("contributions", contributor.getContributions() + "");
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {

            }
        });
    }

    /**
     * 添加请求头
     */
    private void requestGitHubContributorsAddHeader(){

        Call<List<Contributor>> call = mGitHubService.contributorsAndAddHeader(mUserName, mRepo);
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                List<Contributor> contributorList = response.body();
                for (Contributor contributor : contributorList) {
                    Log.d("login", contributor.getLogin());
                    Log.d("contributions", contributor.getContributions() + "");
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {

            }
        });
    }

    /**
     * 同步请求
     */
    private void requestGitHubContributorsBySync(){

        final Call<List<Contributor>> call = mGitHubService.contributorsByAddConverterGetCall(mUserName, mRepo);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Response<List<Contributor>> response = call.execute();

                    List<Contributor> contributorsList = response.body();
                    for (Contributor contributor : contributorsList){
                        Log.d("login",contributor.getLogin());
                        Log.d("contributions",contributor.getContributions()+"");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * get请求
     * @param queryMap
     */
    private void requestQueryRetrofitByGet(Map<String,String> queryMap){
        Call<RetrofitBean> call;
        if (queryMap == null || queryMap.size() == 0){
            call = mGitHubService.queryRetrofitByGetCall("retrofit", "2016-03-29", 1, 3);
        } else {
            call = mGitHubService.queryRetrofitByGetCallMap(queryMap);
        }

        call.enqueue(new Callback<RetrofitBean>() {
            @Override
            public void onResponse(Call<RetrofitBean> call, Response<RetrofitBean> response) {
                RetrofitBean retrofit = response.body();
                List<Item> list = retrofit.getItems();
                if (list == null)
                    return;
                Log.d(TAG, "total:" + retrofit.getTotalCount());
                Log.d(TAG, "incompleteResults:" + retrofit.getIncompleteResults());
                Log.d(TAG, "----------------------");
                for (Item item : list) {
                    Log.d(TAG, "name:" + item.getName());
                    Log.d(TAG, "full_name:" + item.getFull_name());
                    Log.d(TAG, "description:" + item.getDescription());
                    Owner owner = item.getOwner();
                    Log.d(TAG, "login:" + owner.getLogin());
                    Log.d(TAG, "type:" + owner.getType());
                }

            }

            @Override
            public void onFailure(Call<RetrofitBean> call, Throwable t) {

            }
        });
    }

    /**
     * retrofit+rxJava
     */
    private void requestGitHubContributorsByRxJava(){

        mSubscriptions.add(
                mGitHubService.contributorsByRxJava(mUserName, mRepo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Contributor>>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(List<Contributor> contributors) {
                                for (Contributor c : contributors) {
                                    Log.d("TAG", "login:" + c.getLogin() + "  contributions:" + c.getContributions());
                                }
                            }
                        }));
    }

    /**
     * retrofit+rxJava
     */
    private void requestGitHubContributorsWithFullUserInfo(){
        mSubscriptions.add(mGitHubService.contributorsByRxJava(mUserName, mRepo)
                .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
                    @Override
                    public Observable<Contributor> call(List<Contributor> contributors) {
                        return Observable.from(contributors);
                    }
                })
                .flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
                    @Override
                    public Observable<Pair<User, Contributor>> call(Contributor contributor) {
                        Observable<User> userObservable = mGitHubService.userByRxJava(contributor.getLogin())
                                .filter(new Func1<User, Boolean>() {
                                    @Override
                                    public Boolean call(User user) {
                                        return !isEmpty(user.getName()) && !isEmpty(user.getEmail());
                                    }
                                });

                        return Observable.zip(userObservable,
                                Observable.just(contributor),
                                new Func2<User, Contributor, Pair<User, Contributor>>() {
                                    @Override
                                    public Pair<User, Contributor> call(User user, Contributor contributor) {
                                        return new Pair<>(user, contributor);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<User, Contributor>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Pair<User, Contributor> pair) {
                        User user = pair.first;
                        Contributor contributor = pair.second;
                        Log.d(TAG, "name:" + user.getName());
                        Log.d(TAG, "contributions:" + contributor.getContributions());
                        Log.d(TAG, "email:" + user.getEmail());

                    }
                }));
    }

}
