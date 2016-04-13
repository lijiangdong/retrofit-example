package com.ljd.retrofit.progress;

/**
 * Created by ljd on 3/29/16.
 */
public interface ProgressListener {
    /**
     * @param bytesRead     已下载字节数
     * @param contentLength 总字节数
     * @param done          是否下载完成
     */
    void update(long bytesRead, long contentLength, boolean done);
}
