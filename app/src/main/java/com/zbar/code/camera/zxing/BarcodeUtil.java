package com.zbar.code.camera.zxing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.MultiFormatOneDReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Description 条形码工具类
 * Version 1.0
 * Created by Czf on 2019/11/23 15:18
 */
public class BarcodeUtil {

    /**
     * 生成二维码
     */
    public static boolean encodeQR(String contents, int width, int height, String imgPath) {
        return encodeQR(contents, width, height, null, imgPath);
    }

    public static boolean encodeQR(String contents, int width, int height, Bitmap logo, String imgPath) {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        // 指定纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 指定编码格式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置空白边距的宽度
        //hints.put(EncodeHintType.MARGIN, 2); //default is 4
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,
                    BarcodeFormat.QR_CODE, width, height, hints);

            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,

                     90, width, 0, 0, width, height);


            if (logo != null) {
                bitmap = addQRLogo(bitmap, logo);
            }

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(imgPath));

        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 二维码中添加Logo图案
     */
    public static Bitmap addQRLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }


    /**
     * 读取二维码
     */
    public static Result decodeQR(Bitmap bitmap) {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap btp = new BinaryBitmap(new HybridBinarizer(source));
        try {
            return new QRCodeReader().decode(btp, hints);
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * 创建条形码
     */
    public static boolean encodeBar(String contents, int width, int height, String imgPath, boolean isShowContent) {
        //条形码的最小宽度
        int codeWidth = Math.max(98, width);
        int codeHeight = Math.max(30, height);
        try {
            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别 这里选择最高H级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,
                    BarcodeFormat.CODE_128, codeWidth, codeHeight, hints);

            int[] pixels = new int[codeWidth * codeHeight];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < codeHeight; y++) {
                for (int x = 0; x < codeWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * codeWidth + x] = 0xff000000; // 黑色
                    } else {
                        pixels[y * codeWidth + x] = 0xffffffff;// 白色
                    }
                }
            }

            // 生成条形码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(codeWidth, codeHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, codeWidth, 0, 0, codeWidth, codeHeight);

            if (isShowContent) {
                bitmap = showContent(bitmap, contents);
            }

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(imgPath));
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 显示条形的内容
     */
    private static Bitmap showContent(Bitmap barBitmap, String content) {
        if (TextUtils.isEmpty(content) || null == barBitmap) {
            return null;
        }

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(42);

        //测量字符串的宽度
        int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //绘制字符串矩形区域的高度
        int textHeight = (int) (fm.bottom - fm.top);
        // 居中开始X
        float centerStartX = (barBitmap.getWidth() - textWidth) / 2;

        paint.setTextScaleX(1.0F);

        //绘制文本的基线
        int baseLine = barBitmap.getHeight() + textHeight;

        //创建一个图层，然后在这个图层上绘制barBitmap、content
        Bitmap bitmap = Bitmap.createBitmap(barBitmap.getWidth(), barBitmap.getHeight() + textHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(barBitmap, 0, 0, null);
        canvas.drawText(content, centerStartX, baseLine, paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }


    /**
     * 读取条形码
     */
    public static Result decodeBar(Bitmap bitmap) {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        Vector<BarcodeFormat> decodeFormats = new Vector<>();

        //常用在水、电、瓦斯等帐单上。
        decodeFormats.add(BarcodeFormat.CODE_39);

        //又称ITF条码,常用在序号，外箱编号(ITF-14条码)..等应用。
        decodeFormats.add(BarcodeFormat.ITF);

        //我们身份证上所使用的就是Code 128条码,这种条码可以涵盖128个ASCII code字元,包含英文字大小写,数字,还有特殊符号及不可见的电脑符号等。
        decodeFormats.add(BarcodeFormat.CODE_128);

        //属于国际标准条码,在GS1系统称之为GTIN-13条码,我们买的商品上所列印的条码均属于此类条码。
        decodeFormats.add(BarcodeFormat.EAN_13);

        //一样是属于国际标准条码,在GS1系统称之为GTIN-8条码,常用在面积比较小的商品上,例如香烟盒上的条码。
        decodeFormats.add(BarcodeFormat.EAN_8);

        //其它条码︰如Codabar 条码、UPC 条码、Code 93 条码、Code 11 条码、MSI条码、Plessey 条码、Toshiba code 条码、Code 32 条码、RSS 条码 …等

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap btp = new BinaryBitmap(new HybridBinarizer(source));
        try {
            return new MultiFormatOneDReader(hints).decode(btp);
        } catch (Exception ignored) {
        }
        return null;
    }
}
