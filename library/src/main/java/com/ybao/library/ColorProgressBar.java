package com.ybao.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Shader;
import android.util.AttributeSet;

/**
 * colorful arc progress bar
 * Created by Ybao on 16/7/15.
 */
public class ColorProgressBar extends YProgressBar {


    float startBX;
    float stopBX;
    float startFX;
    float stopFX;
    float y;

    private Paint allArcPaint;
    private Paint progressPaint;

    private PaintFlagsDrawFilter mDrawFilter;
    private Shader shader;
//    private Matrix rotateMatrix;

    private float currentX = 0;//当前角度

    private float bgArcWidth = dipToPx(2);
    private float progressWidth = dipToPx(10);

    private int bgArcColor = DEF_BG_ARC_COLOR;
    private int[] colors = new int[]{DEF_PROGRESS_COLOR};

    private final static int DEF_PROGRESS_COLOR = 0xff7eb6e2;
    private final static int DEF_BG_ARC_COLOR = 0xffdddddd;

    // sweepAngle / maxValues 的值
    private float k;

    public ColorProgressBar(Context context) {
        super(context, null);
        initView();
    }

    public ColorProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCofig(context, attrs);
        initView();
    }

    public ColorProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
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

        try {
            colors = new int[]{a.getColor(R.styleable.YProgressBar_front_color, DEF_PROGRESS_COLOR)};
        } catch (Exception e) {
            try {
                String[] colorStrs = a.getString(R.styleable.YProgressBar_front_color).split(",");
                if (colorStrs != null && colorStrs.length > 0) {
                    if (colorStrs.length > 1) {
                        colors = new int[colorStrs.length];
                        for (int i = 0; i < colorStrs.length; i++) {
                            colors[i] = Color.parseColor(colorStrs[i]);
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

        //整个
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);
        allArcPaint.setStrokeWidth(bgArcWidth);
        allArcPaint.setColor(bgArcColor);

        //当前进度
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float width = 0;
        float height = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = 0;
        }

        height = Math.max(progressWidth, bgArcWidth) + getPaddingTop() + getPaddingBottom();

        resetUtils(width);
        ready();

        setMeasuredDimension((int) width, (int) height);
    }

    private void resetUtils(float width) {
        startBX = getPaddingLeft() + bgArcWidth / 2;
        stopBX = width - getPaddingRight() - bgArcWidth / 2;
        startFX = getPaddingLeft() + progressWidth / 2;
        stopFX = width - getPaddingRight() - progressWidth / 2;
        y = getPaddingTop() + Math.max(progressWidth, bgArcWidth) / 2;
        initProgressColors(colors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        currentX = (curValues - minValues) * k + startFX;
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);

        //整个
        canvas.drawLine(startBX, y, stopBX, y, allArcPaint);

        //设置渐变色
//        if (rotateMatrix != null || shader != null) {
//            rotateMatrix.setRotate(-(360 - sweepAngle) / 2 + startAngle, centerX, centerY);
//            shader.setLocalMatrix(rotateMatrix);
//            progressPaint.setShader(shader);
//        }

        //当前进度
        canvas.drawLine(startFX, y, currentX, y, progressPaint);
    }

    /**
     * 设置最大值
     *
     * @param minValues
     * @param maxValues
     */
    public void setMaxValues(float minValues, float maxValues) {
        super.setMaxValues(minValues, maxValues);
        k = (stopFX - startFX) / dValue;
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
            shader = new LinearGradient(startFX, y, stopFX, y, this.colors, null, Shader.TileMode.CLAMP);
//            rotateMatrix = new Matrix();
            progressPaint.setShader(shader);
        } else {
            progressPaint.setColor(this.colors[0]);
            progressPaint.setShader(null);
            shader = null;
//            rotateMatrix = null;
        }

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
}
