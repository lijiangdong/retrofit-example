package com.example.ljd.retrofit;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.text.TextUtils.isEmpty;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button button;
    private GithubApi githubService;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(subscriptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        githubService = GithubService.createGithubService("5a67a1dd3aaee936c981f761e09fa6fbf0e49c95");
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        requestGithubContributors();
        requestGithubContributorsWithFullUserInfo();
//        requestGithubContributors1();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxUtils.unSubscribeIfNotNull(subscriptions);
    }

    private void requestGithubContributors2(){

    }

    private void requestGithubContributors1(){
        Call<List<Contributor>> call = githubService.contributors1("square", "retrofit");
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                Log.e("onResponse是否运行在主线程",(Looper.getMainLooper() == Looper.myLooper())+"");
                Log.e("message",response.message());
                List<Contributor> list = response.body();
                Log.e("string",response.body().toString());
                for (Contributor contributor : list){
                    Log.d("login",contributor.login);
                    Log.d("contributions",contributor.contributions+"");
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {
                Log.e("onFailure是否运行在主线程",(Looper.getMainLooper() == Looper.myLooper())+"");
            }
        });
    }

    private void requestGithubContributors(){
        subscriptions.add(//
                githubService.contributors("square", "retrofit")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Contributor>>() {
                            @Override
                            public void onCompleted() {
                                Log.e("****", "complete");
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(List<Contributor> contributors) {
                                for (Contributor c : contributors) {
                                    Log.e("---", c.login + "   " + c.contributions);
                                }
                            }
                        }));
    }

    private void requestGithubContributorsWithFullUserInfo(){
        subscriptions.add(githubService.contributors("square", "retrofit")
                .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
                    @Override
                    public Observable<Contributor> call(List<Contributor> contributors) {
                        return Observable.from(contributors);
                    }
                })
                .flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
                    @Override
                    public Observable<Pair<User, Contributor>> call(Contributor contributor) {
                        Observable<User> userObservable = githubService.user(contributor.login)
                                .filter(new Func1<User, Boolean>() {
                                    @Override
                                    public Boolean call(User user) {
                                        return !isEmpty(user.name) && !isEmpty(user.email);
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
                        Log.e("****", user.name + "   " + user.email + "   " + contributor.contributions);
                    }
                }));
    }

}
