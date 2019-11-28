package com.zbar.code.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;

import java.io.IOException;


/**
 * Description Camera相关 (: 遵循里氏替换原则，模块互不强依赖
 * Version 1.0
 * Created by Czf on 2019/11/15 18:19
 */
public interface ICamera {

    Camera getCamera();

    Handler getCameraHandler();

    Point getCameraResolution();

    Rect getCropRect(int w, int h);


    interface IAction extends ICamera {

        //开启相机
        void openDriver() throws IOException;

        //开始预览
        void startPreview();

        //停止预览
        void stopPreview();

        //请求预览帧
        void requestPreviewFrame(Handler handler, int what);

        //请求对焦
        void requestAutoFocus(Handler handler, int what);

        //关闭相机
        void closeDriver();


        //灯光
        void openLight();

        void offLight();

        boolean isLight();
    }

    interface Listener {
        /**
         * 相机打开
         */
        void onCameraOpen();

        /**
         * 返回结果
         *
         * @param content 解码内容
         * @return 是否继续扫描
         */
        boolean onResult(String content);

        /**
         * 获得裁剪区域
         *
         * @param w 图片宽度
         * @param h 图片高度
         */
        Rect getCropRect(int w, int h);
    }
}
