package com.example.ljd.retrofit;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.ljd.retrofit.pojo.Contributor;
import com.example.ljd.retrofit.pojo.Item;
import com.example.ljd.retrofit.pojo.Owner;
import com.example.ljd.retrofit.pojo.RetrofitBean;
import com.example.ljd.retrofit.pojo.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
        mGitHubService = GitHubService.createGitHubService();
        mSubscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(mSubscriptions);
        mUserName = getResources().getString(R.string.user_name);
        mRepo = getResources().getString(R.string.repo);
    }

    @OnClick({R.id.btn_retrofit_simple_contributors,
            R.id.btn_add_header_contributors,
            R.id.btn_retrofit_sync_contributors,
            R.id.btn_retrofit_get_query,
            R.id.btn_retrofit_get_query_map,
            R.id.btn_rxJava_retrofit_contributors,
            R.id.btn_rxJava_retrofit_contributors_with_user_info,
            R.id.btn_download_retrofit,
            R.id.btn_post_field,
            R.id.btn_post_field_map
    })

    public void onClickButton(View v){
        Map<String,String> queryMap = new HashMap<>();
        queryMap.put("q", "retrofit");
        queryMap.put("since","2016-03-29");
        queryMap.put("page","1");
        queryMap.put("per_page", "3");
        switch (v.getId()){
            case R.id.btn_retrofit_simple_contributors:
                requestGitHubContributorsSimple();
                break;
            case R.id.btn_retrofit_sync_contributors:
                requestGitHubContributorsBySync();
                break;
            case R.id.btn_add_header_contributors:
                requestAddHeader();
                break;
            case R.id.btn_retrofit_get_query:
                requestQueryRetrofitByGet(null);
                break;
            case R.id.btn_retrofit_get_query_map:
                requestQueryRetrofitByGet(queryMap);
                break;
            case R.id.btn_rxJava_retrofit_contributors:
                requestGitHubContributorsByRxJava();
                break;
            case R.id.btn_rxJava_retrofit_contributors_with_user_info:
                requestGitHubContributorsWithFullUserInfo();
                break;
            case R.id.btn_download_retrofit:
                retrofitDownload();
                break;
            case R.id.btn_post_field:
                requestQueryRetrofitByPost(null);
                break;
            case R.id.btn_post_field_map:
                requestQueryRetrofitByPost(queryMap);
                break;
        }
    }


    public void requestGitHubContributorsSimple(){

        Retrofit retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")
                .build();
        GitHubApi repo = retrofit.create(GitHubApi.class);

        Call<ResponseBody> call = repo.contributorsBySimpleGetCall(mUserName, mRepo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.e(TAG, response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void requestGitHubContributorsBySync(){

        Retrofit retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")
                .build();
        GitHubApi repo = retrofit.create(GitHubApi.class);

        final Call<ResponseBody> call = repo.contributorsBySimpleGetCall(mUserName, mRepo);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Response<ResponseBody> response = call.execute();
                    Log.e(TAG,response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void requestAddHeader(){
        Call<ResponseBody> call = mGitHubService.contributorsAndAddHeader(mUserName, mRepo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.e(TAG,response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void requestQueryRetrofitByGet(Map<String,String> fieldMap){
        Call<RetrofitBean> call;
        if (fieldMap == null || fieldMap.size() == 0){
            call = mGitHubService.queryRetrofitByPostField("retrofit", "2016-03-29", 1, 3);
        } else {
            call = mGitHubService.queryRetrofitByPostFieldMap(fieldMap);
        }

        call.enqueue(new Callback<RetrofitBean>() {
            @Override
            public void onResponse(Call<RetrofitBean> call, Response<RetrofitBean> response) {
                RetrofitBean retrofit = response.body();
                List<Item> list = retrofit.getItems();
                if (list == null)
                    return;
                Log.d(TAG,"total:" + retrofit.getTotalCount());
                Log.d(TAG,"incompleteResults:" + retrofit.getIncompleteResults());
                Log.d(TAG,"----------------------");
                for (Item item : list){
                    Log.d(TAG,"name:"+item.getName());
                    Log.d(TAG,"full_name:"+item.getFull_name());
                    Log.d(TAG,"description:"+item.getDescription());
                    Owner owner = item.getOwner();
                    Log.d(TAG,"login:"+owner.getLogin());
                    Log.d(TAG,"type:"+owner.getType());
                }

            }

            @Override
            public void onFailure(Call<RetrofitBean> call, Throwable t) {

            }
        });
    }

    public void requestQueryRetrofitByPost(Map<String,String> queryMap){
        Call<RetrofitBean> call;
        if (queryMap == null || queryMap.size() == 0){
            call = mGitHubService.queryRetrofitByGetCall("retrofit", "2016-03-29", 1, 3);
        } else {
            call = mGitHubService.queryRetrofitByGetCallMap(queryMap);
        }

        call.enqueue(new Callback<RetrofitBean>() {
            @Override
            public void onResponse(Call<RetrofitBean> call, Response<RetrofitBean> response) {
                RetrofitBean retrofitBean = response.body();
                List<Item> list = retrofitBean.getItems();
                Log.d(TAG,"total:" + retrofitBean.getTotalCount());
                Log.d(TAG,"incompleteResults:" + retrofitBean.getIncompleteResults());
                Log.d(TAG,"----------------------");
                for (Item item : list){
                    Log.d(TAG,"name:"+item.getName());
                    Log.d(TAG,"full_name:"+item.getFull_name());
                    Log.d(TAG,"description:"+item.getDescription());
                    Owner owner = item.getOwner();
                    Log.d(TAG,"login:"+owner.getLogin());
                    Log.d(TAG,"type:"+owner.getType());
                }

            }

            @Override
            public void onFailure(Call<RetrofitBean> call, Throwable t) {

            }
        });
    }

    private void retrofitDownload(){
        //监听下载进度
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                Log.e("-----",(Looper.getMainLooper() == Looper.myLooper()) +"");
                System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
            }
        };
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        //添加拦截器，自定义ResponseBody，添加下载进度
        clientBuilder.networkInterceptors().add(new Interceptor() {
            @Override public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(
                        new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();

            }
        });
        OkHttpClient client =clientBuilder.build();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://msoftdl.360.cn");
        DownloadAndUploadApi retrofit = retrofitBuilder
                .client(client)
                .build().create(DownloadAndUploadApi.class);

        Call<ResponseBody> call = retrofit.retrofitDownload();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    InputStream is = response.body().byteStream();
                    File file = new File(Environment.getExternalStorageDirectory(), "12345.apk");
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len ;
                    int total = 0;
                    while((len =bis.read(buffer))!=-1){
                        fos.write(buffer, 0, len);
                        total += len;
                        fos.flush();
                    }
                    fos.close();
                    bis.close();
                    is.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
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
                                    Log.d("TAG","login:"+c.getLogin()+"  contributions:"+c.getContributions());
                                }
                            }
                        }));
    }

    public void requestGitHubContributorsWithFullUserInfo(){
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
                        Log.d(TAG,"name:"+user.getName());
                        Log.d(TAG,"contributions:"+contributor.getContributions());
                        Log.d(TAG,"email:"+user.getEmail());

                    }
                }));
    }
}
