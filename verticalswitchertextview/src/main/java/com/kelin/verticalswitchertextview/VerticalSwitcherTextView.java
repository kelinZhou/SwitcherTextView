package com.kelin.verticalswitchertextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

/**
 * <strong>描述: </strong> 上下滚动轮播的TextView。
 * <p><strong>创建人: </strong> kelin
 * <p><strong>创建时间: </strong> 2018/7/2  上午9:59
 * <p><strong>版本: </strong> v 1.0.0
 */
public class VerticalSwitcherTextView extends TextSwitcher implements ViewSwitcher.ViewFactory {

    private CharSequence text;
    private String curText;
    private ArrayList<String> lineText = new ArrayList<>(2);
    private float textSize;
    private int textColor;
    private int realWidth;
    private int curIndex;
    private boolean needMeasureText;
    private final SwitcherHandler HANDLER = new SwitcherHandler();
    private int gravity;
    private int intervalDuration;

    public VerticalSwitcherTextView(Context context) {
        this(context, null);
    }

    public VerticalSwitcherTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView);
        intervalDuration = a.getInteger(com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView_intervalDuration, 2000);
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        textSize = a.getDimensionPixelSize(com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView_android_textSize, (int) (0x0e * fontScale + 0.5f));
        textColor = a.getColor(com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView_android_textColor, Color.BLACK);
        CharSequence text = a.getText(com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView_android_text);
        gravity = a.getInteger(com.kelin.verticalswitchertextview.R.styleable.VerticalSwitcherTextView_android_gravity, Gravity.NO_GRAVITY);
        a.recycle();
        setFactory(this);
        setText(text);
    }

    @Override
    public void setText(CharSequence text) {
        this.text = text;
        curText = text == null ? null : text.toString();
        lineText.clear();
        if (TextUtils.isEmpty(curText)) {
            callSuperSetText(null);
            HANDLER.stop();
        } else if (!curText.contains("\n") && measureText(curText) < realWidth) {
            needMeasureText = false;
            callSuperSetText(curText);
            HANDLER.stop();
        } else if (realWidth == 0) {
            needMeasureText = true;
        } else {
            needMeasureText = false;
            onMeasureText(realWidth, curText);
        }
    }

    public CharSequence getText() {
        return text;
    }

    /**
     * 获取行数。
     * @return 返回当前text的行数。
     */
    public int getLineNumber() {
        return lineText.size();
    }

    private void callSuperSetText(String text) {
        super.setText(text);
    }

    private void onMeasureText(int width, String text) {
        if (measureText(text) > width) {
            onMeasureText(width, text.substring(0, text.length() - 1));
        } else {
            String nextLineText;
            int indexOf = text.indexOf("\n");
            if (indexOf > 0) {
                lineText.add(text.substring(0, indexOf));
                nextLineText = curText.substring(indexOf + 1, curText.length());
            } else {
                lineText.add(text);
                nextLineText = curText.substring(text.length(), curText.length());
            }
            if (measureText(nextLineText) < realWidth) {
                if (!TextUtils.isEmpty(nextLineText)) {
                    lineText.add(nextLineText);
                }
                callSuperSetText(lineText.get(0));
                if (lineText.size() > 1) {
                    HANDLER.start();
                }
            } else {
                curText = nextLineText;
                onMeasureText(width, nextLineText);
            }
        }
    }

    private float measureText(String text) {
        return ((TextView) getCurrentView()).getPaint().measureText(text.replace("\n", ""));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        realWidth = w - getPaddingLeft() - getPaddingRight();
        if (needMeasureText) {
            onMeasureText(realWidth, curText);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int parentLeft = getPaddingLeft();
        int parentRight = right - parentLeft - getPaddingRight();
        int parentTop = getPaddingTop();
        int parentBottom = bottom - parentTop - getPaddingBottom();
        getChildAt(0).layout(parentLeft, parentTop, parentRight, parentBottom);
        getChildAt(1).layout(parentLeft, parentTop, parentRight, parentBottom);
    }

    @Override
    public View makeView() {
        TextView t = new TextView(getContext());
        t.setGravity(gravity);
        t.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        t.setMaxLines(1);
        t.setPadding(0, 0, 0, 0);
        t.setTextColor(textColor);
        t.getPaint().setTextSize(textSize);
        return t;
    }

    @SuppressLint("HandlerLeak")
    private class SwitcherHandler extends Handler {
        private SwitcherRunnable curRunnable;
        void start() {
            if (curRunnable != null) {
                removeCallbacks(curRunnable.stop(), null);
            }
            curRunnable = new SwitcherRunnable();
            postDelayed(curRunnable, intervalDuration);
        }

        void stop() {
            if (curRunnable != null) {
                removeCallbacks(curRunnable.stop(), null);
            }
        }
    }

    private class SwitcherRunnable implements Runnable {

        private boolean isFinish = false;

        @Override
        public void run() {
            if (!isFinish) {
                callSuperSetText(lineText.get(++curIndex % lineText.size()));
                HANDLER.start();
            }
        }

        Runnable stop() {
            isFinish = true;
            return this;
        }
    }
}
