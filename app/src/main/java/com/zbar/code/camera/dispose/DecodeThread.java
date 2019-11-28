package com.zbar.code.camera.dispose;

import android.os.Handler;
import android.os.Looper;

import com.zbar.code.camera.ICamera;

import java.util.concurrent.CountDownLatch;

/**
 * Description 解码线程
 * Version 1.0
 * Created by Czf on 2019/11/20 11:22
 */
public final class DecodeThread extends Thread {

    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    private ICamera iCamera;

     DecodeThread(ICamera iCamera) {
        this.iCamera = iCamera;
        this.handlerInitLatch = new CountDownLatch(1);
    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(iCamera);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
