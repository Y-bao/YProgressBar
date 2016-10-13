package com.ybao.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.math.BigDecimal;

/**
 * colorful arc progress bar
 * Created by Ybao on 16/7/15.
 */
public abstract class YProgressBar extends View {

    protected float minValues = 0;//最大值
    protected float maxValues = 100;//最大值
    protected float targetValues = 0;//目标值

    protected float curValues = 0;//当前值

    protected float minIncrement = 1;
    protected float maxIncrement = 3;

    protected float dValue = 0;

    Handler handler = new Handler(Looper.getMainLooper());

    public YProgressBar(Context context) {
        super(context, null);
    }

    public YProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCofig(context, attrs);
    }

    public YProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCofig(context, attrs);
    }

    /**
     * 初始化布局配置
     *
     * @param context
     * @param attrs
     */
    private void initCofig(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YProgressBar);

        maxValues = a.getFloat(R.styleable.YProgressBar_max_value, 100);
        minValues = a.getFloat(R.styleable.YProgressBar_min_value, 0);
        if (minValues > maxValues) {
            throw new RuntimeException("minValues > maxValues");
        }
        curValues = minValues;
        targetValues = chechTargetValues(a.getFloat(R.styleable.YProgressBar_current_value, minValues));
        boolean hasAnim = a.getBoolean(R.styleable.YProgressBar_has_anim, true);
        if (!hasAnim) {
            curValues = targetValues;
        }

        a.recycle();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(runnable);
            if (curValues < targetValues) {
                float addValues = (targetValues - curValues) / dValue * (maxIncrement - minIncrement) + minIncrement;
                curValues += addValues;
                if (curValues > targetValues) {
                    curValues = targetValues;
                }
            } else if (curValues > targetValues) {
                float addValues = (curValues - targetValues) / dValue * (maxIncrement - minIncrement) + minIncrement;
                curValues -= addValues;
                if (curValues < targetValues) {
                    curValues = targetValues;
                }
            }
            invalidate();
            onProgess();
            if (curValues != targetValues) {
                handler.postDelayed(runnable, 16);
            }
        }
    };

    private void onProgess() {
        if (onProgressListener != null && dValue != 0) {
            onProgressListener.onProgess(get2Point((curValues - minValues) / dValue * 100), curValues);
        }
    }

    /**
     * 设置最大值
     *
     * @param minValues
     * @param maxValues
     */
    public void setMaxValues(float minValues, float maxValues) {
        this.minValues = minValues;
        this.maxValues = maxValues;
        if (minValues > maxValues) {
            throw new RuntimeException("minValues > maxValues");
        }
        dValue = maxValues - minValues;
        minIncrement = 0.015f * dValue;
        maxIncrement = 0.03f * dValue;
    }

    protected void ready() {
        setMaxValues(minValues, maxValues);
        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    /**
     * 设置当前值
     *
     * @param targetValues
     */
    public void setCurrentValues(float targetValues) {
        setCurrentValues(targetValues, true);
    }

    /**
     * 设置当前值
     *
     * @param targetValues
     */
    public void setCurrentValues(float targetValues, boolean hasAnim) {
        this.targetValues = chechTargetValues(targetValues);
        if (!hasAnim) {
            curValues = targetValues;
            postInvalidate();
        } else {
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }

    protected float chechTargetValues(float targetValues) {
        if (targetValues > maxValues) {
            targetValues = maxValues;
        } else if (targetValues < minValues) {
            targetValues = minValues;
        }
        return targetValues;
    }

    public interface OnProgressListener {
        void onProgess(float curValuesPs, float curValues);
    }

    private OnProgressListener onProgressListener;

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        onProgess();
    }

    public float get2Point(float value) {
        BigDecimal b = new BigDecimal(value);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}
