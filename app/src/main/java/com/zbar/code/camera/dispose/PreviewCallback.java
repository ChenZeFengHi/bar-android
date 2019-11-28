package com.zbar.code.camera.dispose;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zbar.code.camera.CameraConfigurationManager;


/**
 * Description 相机预览回调
 * Version 1.0
 * Created by Czf on 2019/11/14 17:47
 */
public class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final CameraConfigurationManager configManager;
    private final boolean useOneShotPreviewCallback;
    private Handler handler;
    private int what;

    public PreviewCallback(CameraConfigurationManager configManager, boolean useOneShotPreviewCallback) {
        this.configManager = configManager;
        this.useOneShotPreviewCallback = useOneShotPreviewCallback;
    }


    public void setHandler(Handler handler, int what) {
        this.handler = handler;
        this.what = what;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null);
        }
        if (handler != null) {
            Message message = handler.obtainMessage(what, cameraResolution.x,
                    cameraResolution.y, data);
            message.sendToTarget();
            handler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler for it");
        }
    }
}
