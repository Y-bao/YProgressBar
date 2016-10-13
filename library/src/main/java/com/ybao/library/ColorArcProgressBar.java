package com.ybao.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * colorful arc progress bar
 * Created by Ybao on 16/7/15.
 */
public class ColorArcProgressBar extends YProgressBar {

    private float diameter = 500;  //直径
    private float centerX;  //圆心X坐标
    private float centerY;  //圆心Y坐标

    private Paint allArcPaint;
    private Paint progressPaint;

    private RectF bgRect;

    private PaintFlagsDrawFilter mDrawFilter;
    private Shader shader;
    private Matrix rotateMatrix;

    private float startAngle = 0;//开始角度
    private float sweepAngle = 360;//弧 总角度

    private float currentAngle = 0;//当前角度


    private float bgArcWidth = dipToPx(2);
    private float progressWidth = dipToPx(10);

    private int bgArcColor = DEF_BG_ARC_COLOR;
    private int[] colors = new int[]{DEF_PROGRESS_COLOR};

    private final static int DEF_PROGRESS_COLOR = 0xff7eb6e2;
    private final static int DEF_BG_ARC_COLOR = 0xffdddddd;

    // sweepAngle / maxValues 的值
    private float k;

    public ColorArcProgressBar(Context context) {
        super(context, null);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCofig(context, attrs);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCofig(context, attrs);
        initView();
    }

    /**
     * 初始化布局配置
     *
     * @param context
     * @param attrs
     */
    private void initCofig(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YProgressBar);
        startAngle = a.getInteger(R.styleable.YProgressBar_start_angle, 0);
        sweepAngle = a.getInteger(R.styleable.YProgressBar_sweep_angle, 360);
        diameter = a.getDimension(R.styleable.YProgressBar_diameter, getScreenWidth() * 3 / 5);
        if (sweepAngle > 360) {
            sweepAngle = 360;
        }

        try {
            colors = new int[]{a.getColor(R.styleable.YProgressBar_front_color, DEF_PROGRESS_COLOR)};
        } catch (Exception e) {
            try {
                String[] colorStrs = a.getString(R.styleable.YProgressBar_front_color).split(",");
                if (colorStrs != null && colorStrs.length > 0) {
                    if (colorStrs.length > 1) {

                        String[] doubleColorStrs;
                        doubleColorStrs = new String[colorStrs.length * 2];
                        for (int i = 0; i < doubleColorStrs.length - 1; i++) {
                            doubleColorStrs[i] = colorStrs[(i + 1) / 2];
                        }
                        doubleColorStrs[doubleColorStrs.length - 1] = colorStrs[0];
                        int rountCount = Math.round(360.0f / sweepAngle);
                        colors = new int[doubleColorStrs.length * rountCount];
                        for (int j = 0; j < rountCount; j++) {
                            for (int i = 0; i < doubleColorStrs.length; i++) {
                                colors[j * doubleColorStrs.length + i] = Color.parseColor(doubleColorStrs[i]);
                            }
                        }
                    } else {
                        colors = new int[]{Color.parseColor(colorStrs[0])};
                    }
                }
            } catch (Exception e2) {
                colors = null;
            }
        }


        bgArcColor = a.getColor(R.styleable.YProgressBar_back_color, DEF_BG_ARC_COLOR);


        bgArcWidth = a.getDimension(R.styleable.YProgressBar_back_width, dipToPx(2));
        progressWidth = a.getDimension(R.styleable.YProgressBar_front_width, dipToPx(10));

        a.recycle();
    }

    private void initView() {

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        //整个弧形
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);
        allArcPaint.setStrokeWidth(bgArcWidth);
        allArcPaint.setColor(bgArcColor);

        //当前进度的弧形
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float maxStroke = Math.max(progressWidth, bgArcWidth);

        int width = (int) (maxStroke + diameter);
        int height = width;
        resetUtils();
        ready();

        setMeasuredDimension(width, height);
    }


    private void resetUtils() {
        float maxStroke = Math.max(progressWidth, bgArcWidth);
        //弧形的矩阵区域
        bgRect = new RectF();
        bgRect.top = maxStroke / 2;
        bgRect.left = maxStroke / 2;
        bgRect.right = diameter + maxStroke / 2;
        bgRect.bottom = diameter + maxStroke / 2;

        //圆心
        centerX = (maxStroke + diameter) / 2;
        centerY = (maxStroke + diameter) / 2;

        initProgressColors(colors);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        currentAngle = (curValues - minValues) * k;
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);

        //整个弧
        canvas.drawArc(bgRect, startAngle, sweepAngle, false, allArcPaint);

        //设置渐变色
        if (rotateMatrix != null || shader != null) {
//            rotateMatrix.setRotate(-(360 - sweepAngle) / 2 + startAngle, centerX, centerY);
            rotateMatrix.setRotate(startAngle, centerX, centerY);
            shader.setLocalMatrix(rotateMatrix);
            progressPaint.setShader(shader);
        }

        //当前进度
        canvas.drawArc(bgRect, startAngle, currentAngle, false, progressPaint);
    }


    /**
     * 设置最大值
     *
     * @param minValues
     * @param maxValues
     */
    public void setMaxValues(float minValues, float maxValues) {
        super.setMaxValues(minValues, maxValues);
        k = sweepAngle / dValue;
    }

    public void setBgArcWidth(float bgArcWidth) {
        this.bgArcWidth = bgArcWidth;
        allArcPaint.setStrokeWidth(bgArcWidth);
        requestLayout();
    }

    public void setProgressWidth(float progressWidth) {
        this.progressWidth = progressWidth;
        progressPaint.setStrokeWidth(progressWidth);
        requestLayout();
    }

    public void setStrokeCap(Paint.Cap cap) {
        progressPaint.setStrokeCap(cap);
        allArcPaint.setStrokeCap(cap);
        invalidate();
    }

    public void setBgArcColor(int bgArcColor) {
        this.bgArcColor = bgArcColor;
        allArcPaint.setColor(bgArcColor);
        invalidate();
    }

    public void setProgressColor(int[] colors) {
        initProgressColors(colors);
        invalidate();
    }


    private void initProgressColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            this.colors = colors;
        } else {
            this.colors = new int[]{DEF_PROGRESS_COLOR};
        }

        if (this.colors.length > 1) {
            progressPaint.setColor(Color.GREEN);
            shader = new SweepGradient(centerX, centerY, this.colors, null);
            progressPaint.setShader(shader);
            rotateMatrix = new Matrix();
        } else {
            progressPaint.setColor(this.colors[0]);
            progressPaint.setShader(null);
            shader = null;
            rotateMatrix = null;
        }
    }

    /**
     * 设置直径大小
     *
     * @param diameter
     */
    public void setDiameter(int diameter) {
        this.diameter = diameter;
        requestLayout();
    }


    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 得到屏幕宽度
     *
     * @return
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
