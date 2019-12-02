package com.zbar.code.camera.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.zbar.code.R;

/**
 * Description BarLayer View
 * Version 1.0
 * Created by Czf on 2019/11/25 13:36
 */
public class BarLayerView extends ViewGroup {
    public static final String TAG = BarLayerView.class.getSimpleName();


    /**
     * View Width
     */
    private int measureWidth;
    /**
     * View Height
     */
    private int measureHeight;

    /**
     * BarLayer
     */
    private Rect mBarLayerRect = new Rect();
    public int mBarLayerWidth;
    public int mBarLayerHeight;
    /**
     * center point offset x
     */
    private float offsetX = 0;
    /**
     * center point offset y
     */
    private float offsetY = 0;


    private Paint districtPaint = new Paint();

    public BarLayerView(Context context) {
        super(context);
        init(null);
    }

    public BarLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BarLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BarLayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        float mBarLayerWidth = 0;
        float mBarLayerHeight = 0;
        float offsetX = 0;
        float offsetY = 0;
        if (attrs != null) {
            @SuppressLint("Recycle")
            TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BarLayerView);
            mBarLayerWidth = mTypedArray.getDimension(R.styleable.BarLayerView_bar_width, 280);
            mBarLayerHeight = mTypedArray.getDimension(R.styleable.BarLayerView_bar_height, 280);

            offsetX = mTypedArray.getDimension(R.styleable.BarLayerView_offset_x, 0);
            offsetY = mTypedArray.getDimension(R.styleable.BarLayerView_offset_y, 0);
        }
        this.mBarLayerWidth = (int) mBarLayerWidth;
        this.mBarLayerHeight = (int) mBarLayerHeight;

        this.districtPaint.setColor(0x7F000000);

        this.offsetX = offsetX;
        this.offsetY = offsetY;
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

        int left = (int) (vwc - ((mBarLayerWidth / 2)) + offsetX);
        int top = (int) (vhc - ((mBarLayerHeight / 2)) + offsetY);

        mBarLayerRect.set(left, top, left + mBarLayerWidth, top + mBarLayerHeight);

        Log.d(TAG, mBarLayerRect.toString());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                //child w - h , The xx shall prevail
                int childLeft = mBarLayerRect.left;
                int childTop = mBarLayerRect.top;
                int childRight = mBarLayerRect.right;
                int childBottom = mBarLayerRect.bottom;

                child.layout(childLeft, childTop, childRight, childBottom);

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
        canvas.drawRect(0, 0, measureWidth, mBarLayerRect.top, districtPaint);
        //left
        canvas.drawRect(0, mBarLayerRect.top, mBarLayerRect.left, mBarLayerRect.bottom, districtPaint);
        //right
        canvas.drawRect(mBarLayerRect.right, mBarLayerRect.top, measureWidth, mBarLayerRect.bottom, districtPaint);
        //bottom
        canvas.drawRect(0, mBarLayerRect.bottom, measureWidth, measureHeight, districtPaint);
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
        rect.left = (int) (mBarLayerRect.left * wScale);
        rect.right = (int) (mBarLayerRect.right * wScale);
        rect.top = (int) (mBarLayerRect.top * hScale);
        rect.bottom = (int) (mBarLayerRect.bottom * hScale);
        return rect;
    }
}
