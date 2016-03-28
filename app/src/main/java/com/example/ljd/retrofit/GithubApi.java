package com.example.ljd.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by ljd on 3/25/16.
 */
public interface GithubApi {


    /**
     * See https://developer.github.com/v3/repos/#list-contributors
     */
    @GET("/repos/{owner}/{repo}/contributors")
    Observable<List<Contributor>> contributors(@Path("owner") String owner,
                                               @Path("repo") String repo);

    /**
     * See https://developer.github.com/v3/users/
     */
    @GET("/users/{user}")
    Observable<User> user(@Path("user") String user);


    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors1(@Path("owner") String owner,
                                               @Path("repo") String repo);

    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors2(@Path("owner") String owner,
                                          @Path("repo") String repo);
}
