package com.zbar.code.camera.dispose;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zbar.code.R;
import com.zbar.code.camera.ICamera;

/**
 * Description Camera 消息处理
 * Version 1.0
 * Created by Czf on 2019/11/15 18:31
 */
public final class CameraHandler extends Handler {
    private static final String TAG = CameraHandler.class.getSimpleName();

    private DecodeThread decodeThread;
    private ICamera.IAction iCamera;
    private ICamera.Listener mListener;

    public void setListener(ICamera.Listener mListener) {
        this.mListener = mListener;
    }

    public CameraHandler(ICamera.IAction iAction) {
        this.iCamera = iAction;
        this.decodeThread = new DecodeThread(this.iCamera);
        this.decodeThread.start();
    }

    public DecodeThread getDecodeThread() {
        return decodeThread;
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            Log.i(TAG, "auto focus");
            iCamera.requestAutoFocus(this, R.id.auto_focus);
        } else if (message.what == R.id.restart_preview) {
            iCamera.requestAutoFocus(this, R.id.auto_focus);
            iCamera.requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
        } else if (message.what == R.id.decode_succeeded) {
            Log.i(TAG, "qr_code:" + message.obj);
            boolean isContinue = mListener.onResult(String.valueOf(message.obj));
            if (isContinue) {
                iCamera.requestPreviewFrame(decodeThread.getHandler(),
                        R.id.decode);
            }
        } else if (message.what == R.id.decode_failed) {
            iCamera.requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
        }
    }


    /**
     * exit
     */
    public void quitSynchronously() {
        Message.obtain(decodeThread.getHandler(), R.id.quit).sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        // 确保不会发送任何队列消息
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }
}