package com.zbar.code.camera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

/**
 * Description Bar区域View
 * Version 1.0
 * Created by Czf on 2019/11/25 13:36
 */
public class BarDistrictView extends ViewGroup {
    public static final String TAG = BarDistrictView.class.getSimpleName();
    public int districtWidth;
    public int districtHeight;

    private Rect districtRect = new Rect();

    private Paint districtPaint = new Paint();

    private int measureWidth;
    private int measureHeight;

    public BarDistrictView(Context context) {
        super(context);
        init();
    }

    public BarDistrictView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarDistrictView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BarDistrictView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        districtWidth = (int) (280 * density);
        districtHeight = (int) (280 * density);

        districtPaint.setColor(0x7F000000);

        setWillNotDraw(false);
    }

    /**
     * MeasureSpec.AT_MOST = -2147483648 [0x80000000];  // The child can be as large as it wants up to the specified size.
     * MeasureSpec.EXACTLY = 1073741824 [0x40000000];   // The parent has determined an exact size for the child. The child is going to be given those bounds regardless of how big it wants to be.
     * MeasureSpec.UNSPECIFIED = 0 [0x0];               // The parent has not imposed any constraint on the child. It can be whatever size it wants.
     * <p>
     * 父布局LinearLayout固定width,height是match_parent（其实如果设置为wrap_content它的宽高也是等于屏幕宽高）
     * 1、当宽或高设为确定值时：即width=20dp，height=30dp，或者为match_parent。它会使用MeasureSpec.EXACTLY测量模式（表示父控件已经确切的指定了子View的大小）
     * 2、 当宽或高设为wrap_content时，它会使用MeasureSpec.AT_MOST测量模式（表示子View具体大小没有尺寸限制，但是存在上限，上限一般为父View大小）
     * 3、MeasureSpec.UNSPECIFIED，一般是在特殊情况下出现，如在父布局是ScrollView中才会出现这种测量模式
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, widthMeasureSpec + "-" + heightMeasureSpec);

        measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthModel = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightModel = MeasureSpec.getMode(heightMeasureSpec);

        Log.d(TAG, String.format("wSpec:%d hSpec:%d", widthMeasureSpec, heightMeasureSpec));
        Log.d(TAG, String.format("measureWidth:%d measureHeight:%d", measureWidth, measureHeight));
        Log.d(TAG, String.format("measureWidthModel:%d measureHeightModel:%d", measureWidthModel, measureHeightModel));


        String model = "";
        switch (measureWidthModel) {
            case MeasureSpec.AT_MOST:
                // wrap_content
                model = "measureWidthModel:MeasureSpec.AT_MOST";
                break;
            case MeasureSpec.EXACTLY:
                // match_parent或者 一个具体的值
                model = "measureWidthModel:MeasureSpec.EXACTLY";
                break;
            case MeasureSpec.UNSPECIFIED:
                // 父控件没有给子view任何限制，子View可以设置为任意大小
                model = "measureWidthModel:MeasureSpec.UNSPECIFIED";
                break;
        }
        switch (measureHeightModel) {
            case MeasureSpec.AT_MOST:
                // wrap_content
                model += "measureHeightModel:MeasureSpec.AT_MOST";
                break;
            case MeasureSpec.EXACTLY:
                // match_parent或者 一个具体的值
                model += "measureHeightModel:MeasureSpec.EXACTLY";
                break;
            case MeasureSpec.UNSPECIFIED:
                // 父控件没有给子view任何限制，子View可以设置为任意大小
                model += "measureHeightModel:MeasureSpec.UNSPECIFIED";
                break;
        }
        Log.d(TAG, model);

        int vwc = measureWidth / 2;
        int vhc = measureHeight / 2;

        int left = vwc - ((districtWidth / 2));
        int top = vhc - ((districtHeight / 2));

        districtRect.set(left, top, left + districtWidth, top + districtHeight);

        Log.d(TAG, districtRect.toString());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int childLeft = districtRect.left + districtWidth / 2 - child.getMeasuredWidth() / 2;
                int childTop = districtRect.top + districtWidth / 2 - child.getMeasuredHeight() / 2;

                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());

                Log.d(TAG, String.format("\nchange:%b\nleft:%d\ntop:%d\nright:%d\nbottom:%d\nchildCount:%d", changed, l, t, r, b, childCount));

                Log.d(TAG, String.format("\nMeasuredWidth:%d\nMeasuredHeight:%d", child.getMeasuredWidth(), child.getMeasuredHeight()));
            }
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw:执行了");

        //top
        canvas.drawRect(0, 0, measureWidth, districtRect.top, districtPaint);
        //left
        canvas.drawRect(0, districtRect.top, districtRect.left, districtRect.bottom, districtPaint);
        //right
        canvas.drawRect(districtRect.right, districtRect.top, measureWidth, districtRect.bottom, districtPaint);
        //bottom
        canvas.drawRect(0, districtRect.bottom, measureWidth, measureHeight, districtPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.d(TAG, "draw:执行了");
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d(TAG, "dispatchDraw:执行了");
    }

    /**
     * 获取图片在扫描区域的位置
     */
    public Rect getImageRect(int w, int h) {
        //image and view , width or height unlikeness , calculate the scale value
        float wScale = w / (float) measureWidth;
        float hScale = h / (float) measureHeight;

        Rect rect = new Rect();
        rect.left = (int) (districtRect.left * wScale);
        rect.right = (int) (districtRect.right * wScale);
        rect.top = (int) (districtRect.top * hScale);
        rect.bottom = (int) (districtRect.bottom * hScale);
        return rect;
    }
}
