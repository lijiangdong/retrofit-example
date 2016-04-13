package com.ljd.retrofit.progress;

/**
 * Created by ljd on 4/12/16.
 */
public class ProgressBean {

    private long bytesRead;
    private long contentLength;
    private boolean done;

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
