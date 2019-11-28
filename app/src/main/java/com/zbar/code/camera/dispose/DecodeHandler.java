package com.zbar.code.camera.dispose;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zbar.code.R;
import com.zbar.code.camera.CameraManager;
import com.zbar.code.camera.ICamera;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


/**
 * Description 接受消息处理解码
 * Version 1.0
 * Created by Czf on 2019/11/20 11:21
 */
final class DecodeHandler extends Handler {
    private ImageScanner mImageScanner;
    private ICamera iCamera;

    DecodeHandler(ICamera iCamera) {
        this.iCamera = iCamera;
        this.mImageScanner = new ImageScanner();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);

        } else if (message.what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    private void decode(byte[] data, int width, int height) {
        Log.d("decode:", String.valueOf(data));

        Camera.Size size = CameraManager.get().getCamera().getParameters().getPreviewSize();

        //这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x
                        + y * size.width];
        }
        // 宽高也要调整
        int tmp = size.width;
        size.width = size.height;
        size.height = tmp;

        Rect sanPictureRect = iCamera.getCropRect(size.width, size.height);

        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(rotatedData);
        if (sanPictureRect != null) {
            barcode.setCrop(sanPictureRect.left, sanPictureRect.top, sanPictureRect.width(),
                    sanPictureRect.height());
        }

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        if (!TextUtils.isEmpty(resultStr)) {
            Message msg = new Message();
            msg.obj = resultStr;
            msg.what = R.id.decode_succeeded;
            iCamera.getCameraHandler().sendMessage(msg);
        } else {
            iCamera.getCameraHandler().sendEmptyMessage(R.id.decode_failed);
        }
    }
}
