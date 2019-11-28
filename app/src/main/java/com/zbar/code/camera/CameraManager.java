package com.zbar.code.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zbar.code.R;
import com.zbar.code.camera.dispose.AutoFocusCallBack;
import com.zbar.code.camera.dispose.CameraHandler;
import com.zbar.code.camera.dispose.PreviewCallback;

import java.io.IOException;

/**
 * Description Camera Manager
 * Version 1.0
 * Created by Czf on 2019/11/14 10:35
 */
public final class CameraManager implements ICamera, ICamera.IAction {
    private static final String TAG = CameraManager.class.getSimpleName();

    private static CameraManager cameraManager;

    private Camera camera;
    private final CameraConfigurationManager configManager;

    private boolean initialized;
    private boolean previewing;
    private boolean light = false;


    private final PreviewCallback previewCallback;
    private final AutoFocusCallBack autoFocusCallback;

    private CameraHandler cameraHandler;

    private Listener mListener;

    public static void init(Context mContext) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(mContext);
        }
    }

    synchronized public static CameraManager get() {
        if (cameraManager == null) throw new NullPointerException("cameraManager == null");
        return cameraManager;
    }

    /**
     * Set Listener Must be before openDriver
     */
    public void setListener(Listener mListener) {
        if (cameraHandler != null) {
            cameraHandler.setListener(this.mListener = mListener);
        }
    }

    private CameraManager(@NonNull Context context) {
        //init config
        configManager = new CameraConfigurationManager(context);

        //init handler
        cameraHandler = new CameraHandler(this);

        //init callback
        previewCallback = new PreviewCallback(configManager, Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE);
        autoFocusCallback = new AutoFocusCallBack();
    }

    @Override
    public void requestPreviewFrame(Handler handler, int what) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, what);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    @Override
    public void requestAutoFocus(Handler handler, int what) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, what);
            camera.autoFocus(autoFocusCallback);
        }
    }

    synchronized Camera.AutoFocusCallback getAutoFocusCallback() {
        return autoFocusCallback;
    }

    synchronized Camera.PreviewCallback getPreviewCallback() {
        return previewCallback;
    }


    @Override
    synchronized public void openDriver() throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = Camera.open();
            Log.d(TAG, "OpenCamera!");
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;

            //camera open callback
            if (this.mListener != null) {
                this.mListener.onCameraOpen();
            }
        }
        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(camera);
        }

        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters
                .flatten();
        // Save
        // these,
        // temporarily
        try {
            configManager.setDesiredCameraParameters(camera, false);
        } catch (RuntimeException ex) {
            // Driver failed
            Log.w(TAG,
                    "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: "
                    + parametersFlattened);

            // Reset:
            if (parametersFlattened != null) {
                parameters = theCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    theCamera.setParameters(parameters);
                    configManager.setDesiredCameraParameters(theCamera, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    Log.w(TAG,
                            "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }
    }

    /**
     * if start failure,the reset openDriver
     */
    @Override
    synchronized public void startPreview() {
        if (camera != null && !previewing) {
            try {
                camera.startPreview();
                previewing = true;
            } catch (Exception ex) {
                //解决camera长时间在后台导致被回收或被其他应用占用camera
                //这两种情况都会在Camera内部触发onError导致再次startPreview时抛出异常，so需要重启camera！
                Log.i(TAG, "exception:" + ex.getMessage());
                Log.i(TAG, "reset start camera ...");
                try {
                    camera = null;
                    openDriver();

                    camera.startPreview();
                    previewing = true;
                } catch (IOException ignored) {
                }

            } finally {
                if (previewing) {
                    previewCallback.setHandler(cameraHandler.getDecodeThread().getHandler(), R.id.decode);
                    autoFocusCallback.setHandler(cameraHandler, R.id.auto_focus);
                } else {
                    Log.i(TAG, "camera open error!");
                }
            }
        }
    }

    @Override
    synchronized public void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewing = false;

            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
        }
    }

    @Override
    synchronized public void closeDriver() {

        cameraHandler.quitSynchronously();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void gc() {
        cameraManager = null;
    }

    @Override
    public void openLight() {
        if (camera != null) {
            light = true;
            Camera.Parameters parameter = camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameter);
        }
    }

    @Override
    synchronized public void offLight() {
        if (camera != null) {
            light = false;
            Camera.Parameters parameter = camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameter);
        }
    }

    @Override
    synchronized public Point getCameraResolution() {
        return configManager.getCameraResolution();
    }

    @Override
    public Rect getCropRect(int w, int h) {
        return mListener != null ? mListener.getCropRect(w, h) : null;
    }

    @Override
    synchronized public Camera getCamera() {
        return camera;
    }

    @Override
    synchronized public Handler getCameraHandler() {
        return cameraHandler;
    }

    @Override
    synchronized public boolean isLight() {
        return light;
    }
}
