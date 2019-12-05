package com.zbar.code;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.zbar.code.camera.CameraManager;
import com.zbar.code.camera.CameraPreview;
import com.zbar.code.camera.ICamera;
import com.zbar.code.camera.view.BarLayerView;
import com.zbar.code.camera.beep.BeepSound;
import com.zbar.code.camera.zxing.BarcodeUtil;
import com.zbar.code.camera.zxing.PictureScan;

import java.io.File;
import java.io.IOException;

/**
 * Description 扫描功能
 * Version 1.0
 * Created by Czf on 2019/11/19 13:39
 */
public class MainActivity extends AppCompatActivity implements ICamera.Listener {
    CameraPreview mCameraPreview;
    BarLayerView mBarLayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBarLayerView = findViewById(R.id.bar_layer_view);

        BeepSound.init(this);
        getLifecycle().addObserver(BeepSound.get());

        (((FrameLayout) findViewById(R.id.lay_frame))).addView(mCameraPreview = new CameraPreview(this));

        CameraManager.init(getApplicationContext());

        //Set Listener Must be before openDriver

        //Need Use onCameraOpen Callback
        CameraManager.get().setListener(this);

        try {
            CameraManager.get().openDriver();
        } catch (IOException ignored) {
        }

    }

    private void initScannerAnimation() {
        ImageView mQrLineView = findViewById(R.id.iv_capture_scan_line);
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1200);
        mQrLineView.startAnimation(animation);
    }

    @Override
    public void onCameraOpen() {
        mCameraPreview.setCameraManager(CameraManager.get());
        initScannerAnimation();
    }

    @Override
    public boolean onResult(String content) {
        BeepSound.get().play();
        showContent(content);
        return true;
    }

    @Override
    public Rect getCropRect(int w, int h) {
        Rect rect = mBarLayerView.getImageRect(w, h);
        //Log.d("getCropRect", String.format("\nl:%d\nr:%d\nt:%d\nb:%d", rect.left, rect.right, rect.top, rect.bottom));
        return rect;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //here start preview , if start failure,the reset openDriver
        CameraManager.get().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //here stop preview
        CameraManager.get().stopPreview();
    }

    @Override
    protected void onDestroy() {

        //here remove close GC
        getLifecycle().removeObserver(BeepSound.get());
        BeepSound.get().gc();

        CameraManager.get().closeDriver();
        CameraManager.get().gc();

        super.onDestroy();
    }

    public void click(View view) {
        int i = view.getId();
        if (i == R.id.tv_light) {
            light();
        } else if (i == R.id.tv_picture) {
            PictureScan.StartPicture(this);
        } else if (i == R.id.tv_encode_bar) {
            String fileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "bar.png";
            if (BarcodeUtil.encodeBar("123456789", 500, 200, fileName, true)) {
                Toast.makeText(this, String.format("条形码已保存至 %s", fileName), Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.tv_encode_qr) {
            String fileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "qr.jpg";
            if (BarcodeUtil.encodeQR("QR:123456789", 300, 300, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "qr.jpg")) {
                Toast.makeText(this, String.format("二维码已保存至 %s", fileName), Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.tv_bar_scan) {
            mBarLayerView.useBarScan();
        } else if (i == R.id.tv_qr_scan) {
            mBarLayerView.useQRScan();
        }
    }

    /**
     * 闪光灯
     */
    private void light() {
        if (CameraManager.get().isLight()) {
            CameraManager.get().offLight();
        } else {
            CameraManager.get().openLight();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PictureScan.REQUEST_CODE) {

            PictureScan.ObtainResult(this, data, new PictureScan.ResultListener() {
                @Override
                public void onResult(String result) {
                    showContent(result);
                }

                @Override
                public void onFailure(String info) {
                    showContent(info);
                }
            });
        }
    }


    /**
     * 弹出结果
     */
    void showContent(String content) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(content);
        dialog.setTitle("温馨提示");
        dialog.setNegativeButton("确定", null);
        dialog.show();
    }

}
