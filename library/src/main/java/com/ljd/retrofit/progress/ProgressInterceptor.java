package com.ljd.retrofit.progress;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by ljd on 4/18/16.
 */
public class ProgressInterceptor implements Interceptor {
    private ProgressBean mProgressBean;
    private ProgressHandler mProgressHandler;

    final ProgressListener progressListener = new ProgressListener() {
        //该方法在子线程中运行
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            Log.d("progress:",String.format("%d%% done\n",(100 * bytesRead) / contentLength));

            if (mProgressHandler == null){
                return;
            }

            mProgressBean.setBytesRead(bytesRead);
            mProgressBean.setContentLength(contentLength);
            mProgressBean.setDone(done);
            mProgressHandler.sendMessage(mProgressBean);

        }
    };

    public ProgressInterceptor(ProgressHandler progressHandler){
        mProgressBean = new ProgressBean();
        this.mProgressHandler = progressHandler;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
                new ProgressResponseBody(response.body(),progressListener)
        ).build();
    }
}
