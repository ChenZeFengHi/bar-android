package com.zbar.code.camera.dispose;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Description 相机自动对焦
 * Version 1.0
 * Created by Czf on 2019/11/14 17:42
 */
public class AutoFocusCallBack implements Camera.AutoFocusCallback {
    private static final String TAG = AutoFocusCallBack.class.getSimpleName();
    private Handler handler;
    private int what;

    public void setHandler(Handler handler, int what) {
        this.handler = handler;
        this.what = what;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (handler != null) {
            Message message = handler.obtainMessage(what, success);
            handler.sendMessageDelayed(message, 3000);
            handler = null;
        } else {
            Log.d(TAG, "Got auto-focus callback, but no handler for it");
        }
    }
}
