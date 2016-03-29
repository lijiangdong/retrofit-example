package com.example.ljd.retrofit;

import com.example.ljd.retrofit.pojo.Contributor;
import com.example.ljd.retrofit.pojo.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by ljd on 3/25/16.
 */
public interface GitHubApi {


    @GET("/repos/{owner}/{repo}/contributors")
    Observable<List<Contributor>> contributorsByRxJava(@Path("owner") String owner,
                                               @Path("repo") String repo);

    @GET("/users/{user}")
    Observable<User> userByRxJava(@Path("user") String user);


    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributorsByGetCall(@Path("owner") String owner,
                                               @Path("repo") String repo);


    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors2(@Path("owner") String owner,
                                          @Path("repo") String repo);
}
