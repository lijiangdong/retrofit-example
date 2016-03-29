package com.example.ljd.retrofit.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ljd.retrofit.Contributor;
import com.example.ljd.retrofit.GitHubApi;
import com.example.ljd.retrofit.GitHubService;
import com.example.ljd.retrofit.R;
import com.example.ljd.retrofit.RxUtils;
import com.example.ljd.retrofit.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

/**
 * A simple {@link Fragment} subclass.
 */
public class RxJavaRetrofitFragment extends Fragment {

    @Bind(R.id.demo_retrofit_contributors_username)
    EditText mUsername;
    @Bind(R.id.demo_retrofit_contributors_repository)
    EditText mRepo;
    @Bind(R.id.log_list)
    ListView mResultList;

    private ArrayAdapter<String> mAdapter;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private GitHubApi gitHubService;

    public RxJavaRetrofitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String githubToken = getResources().getString(R.string.github_token);
        gitHubService = GitHubService.createGitHubService(githubToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_rx_java_retrofit, container, false);
        ButterKnife.bind(this,layout);
        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<String>());
        mResultList.setAdapter(mAdapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(subscriptions);
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unSubscribeIfNotNull(subscriptions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_demo_retrofit_contributors)
    public void requestGitHubContributors(){
        subscriptions.add(//
                gitHubService.contributors(mUsername.getText().toString(), mRepo.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Contributor>>() {
                            @Override
                            public void onCompleted() {
                                Timber.d("Retrofit call 1 completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "woops we got an error while getting the list of contributors");
                            }

                            @Override
                            public void onNext(List<Contributor> contributors) {
                                for (Contributor c : contributors) {
                                    mAdapter.add(format("%s has made %d contributions to %s",
                                            c.login,
                                            c.contributions,
                                            mRepo.getText().toString()));

                                    Timber.d("%s has made %d contributions to %s",
                                            c.login,
                                            c.contributions,
                                            mRepo.getText().toString());
                                }
                            }
                        }));
    }

    @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
    public void requestGitHubContributorsWithFullUserInfo(){
        subscriptions.add(gitHubService.contributors("square", "retrofit")
                .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
                    @Override
                    public Observable<Contributor> call(List<Contributor> contributors) {
                        return Observable.from(contributors);
                    }
                })
                .flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
                    @Override
                    public Observable<Pair<User, Contributor>> call(Contributor contributor) {
                        Observable<User> userObservable = gitHubService.user(contributor.login)
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
                        Timber.d("Retrofit call 2 completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "error while getting the list of contributors along with full " + "names");
                    }

                    @Override
                    public void onNext(Pair<User, Contributor> pair) {
                        User user = pair.first;
                        Contributor contributor = pair.second;

                        mAdapter.add(format("%s(%s) has made %d contributions to %s",
                                user.name,
                                user.email,
                                contributor.contributions,
                                mRepo.getText().toString()));
                        mAdapter.notifyDataSetChanged();

                        Timber.d("%s(%s) has made %d contributions to %s",
                                user.name,
                                user.email,
                                contributor.contributions,
                                mRepo.getText().toString());

                    }
                }));
    }
}
