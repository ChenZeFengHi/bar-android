package com.zbar.code.camera.zxing;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.zxing.Result;


/**
 * Description 照片扫描
 * Version 1.0
 * Created by Czf on 2019/11/23 11:17
 */
public class PictureScan {

    public static final int REQUEST_CODE = 11010;

    public static void StartPicture(Activity mActivity) {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setType("image/*");
        mActivity.startActivityForResult(openAlbumIntent, REQUEST_CODE);
    }

    public static void ObtainResult(final Activity mActivity, @NonNull Intent data, final ResultListener listener) {
        final ContentResolver cr = mActivity.getContentResolver();
        // 照片的原始资源地址
        final Uri uri = data.getData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, uri);

                    // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                    Bitmap smallBitmap = ZoomBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                    bitmap.recycle();

                    Result result = BarcodeUtil.decodeQR(smallBitmap);
                    if (result == null) {
                        result = BarcodeUtil.decodeBar(smallBitmap);
                    }
                    final Result finalResult = result;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalResult != null) {
                                listener.onResult(finalResult.toString());
                            } else {
                                listener.onFailure("Fail!");
                            }
                        }
                    });

                } catch (Exception ex) {
                    listener.onFailure(ex.getMessage());
                }
            }
        }).start();
    }


    /**
     * Resize the bitmap
     */
    private static Bitmap ZoomBitmap(Bitmap bitmap, int width, int height) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public interface ResultListener {
        void onResult(String result);

        void onFailure(String info);
    }
}
