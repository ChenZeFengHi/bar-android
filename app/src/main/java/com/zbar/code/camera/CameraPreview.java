package com.zbar.code.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.*;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.io.IOException;

/**
 * Description CameraPreview SurfaceView
 * Version 1.0
 * Created by Czf on 2019/11/14 11:28
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraManager.class.getSimpleName();

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback previewCallback;
    private Camera.AutoFocusCallback autoFocusCallback;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    public void setAutoFocusCallback(Camera.AutoFocusCallback autoFocusCallback) {
        this.autoFocusCallback = autoFocusCallback;
        /*
         * Set camera to continuous focus if supported, otherwise use software
         * auto-focus. Only works for API level >=9.
         */
    }

    public void setHolder() {
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        if (mCamera == null) {
            // mCamera does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            // Hard code camera surface rotation 90 degs to match Activity view
            // in portrait
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(mHolder);
            if (previewCallback != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE) {
                    mCamera.setOneShotPreviewCallback(previewCallback);
                } else {
                    mCamera.setPreviewCallback(previewCallback);
                }
            }
            mCamera.startPreview();
            if (autoFocusCallback != null)
                mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }

    public void setCameraManager(CameraManager mCameraManager) {
        setAutoFocusCallback(mCameraManager.getAutoFocusCallback());
        setPreviewCallback(mCameraManager.getPreviewCallback());
        setCamera(mCameraManager.getCamera());
        setHolder();
    }
}
