package com.zbar.code.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Description 相机配置管理
 * Version 1.0
 * Created by Czf on 2019/11/14 15:24
 */
public final class CameraConfigurationManager {
    private static final String TAG = CameraConfigurationManager.class.getSimpleName();

    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    private final Context context;

    // 屏幕分辨率
    private Point screenResolution;
    // 相机分辨率
    private Point cameraResolution;

    public CameraConfigurationManager(Context context) {
        this.context = context;
    }

    public void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        theScreenResolution = getDisplaySize(display);

        screenResolution = theScreenResolution;

        /** 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;

        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }

        Log.d(TAG, "Screen resolution: " + screenResolutionForCamera);
        cameraResolution = findBestPreviewSizeValue(parameters,
                screenResolutionForCamera);
        //cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
        Log.d(TAG, "Camera resolution: " + cameraResolution);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    public void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG,
                    "In camera config safe mode -- most settings will not be honored");
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null
                && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size "
                    + cameraResolution.x + 'x' + cameraResolution.y
                    + ", but after setting it, preview size is "
                    + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }

        /** 设置相机预览为竖屏 */
        camera.setDisplayOrientation(90);
    }

    public Point getCameraResolution() {
        return cameraResolution;
    }

    public Point getScreenResolution() {
        return screenResolution;
    }

    /**
     * 从相机支持的分辨率中计算出最适合的预览界面尺寸 (方式一)
     *
     * @param parameters
     * @param screenResolution
     * @return
     */
    private Point findBestPreviewSizeValue(Camera.Parameters parameters,
                                           Point screenResolution) {
        List<Camera.Size> rawSupportedSizes = parameters
                .getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG,
                    "Device returned no supported preview sizes; using default");
            Camera.Size defaultSize = parameters.getPreviewSize();
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
                rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        if (Log.isLoggable(TAG, Log.INFO)) {
            StringBuilder previewSizesString = new StringBuilder();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width)
                        .append('x').append(supportedPreviewSize.height)
                        .append(' ');
            }
            Log.i(TAG, "Supported preview sizes: " + previewSizesString);
        }

        double screenAspectRatio = (double) screenResolution.x
                / (double) screenResolution.y;

        // Remove sizes that are unsuitable
        Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewSize = it.next();
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight
                    : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth
                    : realHeight;

            double aspectRatio = (double) maybeFlippedWidth
                    / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            if (maybeFlippedWidth == screenResolution.x
                    && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                Log.i(TAG, "Found preview size exactly matching screen size: "
                        + exactPoint);
                return exactPoint;
            }
        }

        // If no exact match, use largest preview size. This was not a great
        // idea on older devices because
        // of the additional computation needed. We're likely to get here on
        // newer Android 4+ devices, where
        // the CPU is much more powerful.
        if (!supportedPreviewSizes.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewSizes.get(0);
            Point largestSize = new Point(largestPreview.width,
                    largestPreview.height);
            Log.i(TAG, "Using largest suitable preview size: " + largestSize);
            return largestSize;
        }

        // If there is nothing at all suitable, return current preview size
        Camera.Size defaultPreview = parameters.getPreviewSize();
        Point defaultSize = new Point(defaultPreview.width,
                defaultPreview.height);
        Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);

        return defaultSize;
    }

    /**
     * 从相机支持的分辨率中计算出最适合的预览界面尺寸 (方式二)
     *
     * @param parameters
     * @param screenResolution
     * @return
     */
    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
        String previewSizeValueString = parameters.get("preview-size-values");
        // saw this on Xperia
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null) {
            Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
            cameraResolution = new Point(
                    (screenResolution.x >> 3) << 3,
                    (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }

    private static Point findBestPreviewSizeValue(
            CharSequence previewSizeValueString, Point screenResolution) {
        Pattern COMMA_PATTERN = Pattern.compile(",");
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x)
                    + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                //可以限制屏幕比例  如（16:9||4:3）：(16 * newX) == (9 * newY) || (4 * newX) == (3 * newY)
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }
}
