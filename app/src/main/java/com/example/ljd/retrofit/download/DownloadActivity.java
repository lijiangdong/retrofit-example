
package com.example.ljd.retrofit.download;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ljd.retrofit.R;
import com.ljd.retrofit.progress.ProgressListener;
import com.ljd.retrofit.progress.ProgressResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

public class DownloadActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @OnClick(R.id.start_download_btn)
    public void onClickButton(){
        retrofitDownload();
    }

    private void retrofitDownload(){
        //监听下载进度
        final ProgressListener progressListener = new ProgressListener() {
            //该方法在子线程中运行，不能进行UI操作
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
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
        DownloadApi retrofit = retrofitBuilder
                .client(client)
                .build().create(DownloadApi.class);

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
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        fos.flush();
                    }
                    fos.close();
                    bis.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
}
