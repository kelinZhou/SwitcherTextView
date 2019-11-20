package com.kelin.switchertextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher

import java.util.ArrayList

/**
 * **描述: ** 上下滚动轮播的TextView。
 *
 * **创建人: ** kelin
 *
 * **创建时间: ** 2018/7/2  上午9:59
 *
 * **版本: ** v 1.0.0
 */
class SwitcherTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TextSwitcher(context, attrs), ViewSwitcher.ViewFactory {

    private var text: CharSequence? = null
    private var curLineText: String? = null
    private val lineText = ArrayList<String>(2)
    private val textSize: Float
    private val textColor: Int
    private var realWidth: Int = 0
    private var curIndex: Int = 0
    private var needMeasureText: Boolean = false
    private val switcherHandler by lazy { SwitcherHandler() }
    private val gravity: Int
    private val intervalDuration: Int

    /**
     * 获取行数。
     *
     * @return 返回当前text的行数。
     */
    val lineNumber: Int
        get() = lineText.size

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SwitcherTextView)
        intervalDuration = a.getInteger(R.styleable.SwitcherTextView_intervalDuration, 2000)
        val fontScale = getContext().resources.displayMetrics.scaledDensity
        textSize = a.getDimensionPixelSize(R.styleable.SwitcherTextView_android_textSize, (0x0e * fontScale + 0.5f).toInt()).toFloat()
        textColor = a.getColor(R.styleable.SwitcherTextView_android_textColor, Color.BLACK)
        val text = a.getText(R.styleable.SwitcherTextView_android_text)
        gravity = a.getInteger(R.styleable.SwitcherTextView_android_gravity, Gravity.NO_GRAVITY)
        a.recycle()
        setFactory(this)
        setText(text)
    }

    override fun setText(text: CharSequence?) {
        if (!TextUtils.equals(this.text, text)) {
            this.text = text
            val curText = text?.toString()
            lineText.clear()
            if (curText.isNullOrEmpty()) {
                callSuperSetText(null)
                switcherHandler.stop()
            } else if (!curText.contains("\n") && measureText(curText) < realWidth) {
                needMeasureText = false
                callSuperSetText(curText)
                switcherHandler.stop()
            } else if (realWidth == 0) {
                needMeasureText = true
            } else {
                needMeasureText = false
                onMeasureText(curText)
            }
        }
    }

    fun getText(): CharSequence? {
        return text
    }

    private fun callSuperSetText(text: String?) {
        super.setText(text)
    }

    private fun onMeasureText(text: String) {
        var t = text
        if (t.contains("\n")) {
            var line: String
            var indexOf = t.indexOf("\n")
            while (indexOf > 0) {
                line = t.substring(0, indexOf)
                t = t.substring(indexOf + 1)
                val lineWidth = measureText(line).toInt()
                if (lineWidth < realWidth) {
                    curLineText = ""
                    lineText.add(line)
                    checkUpdateView()
                } else {
                    curLineText = line
                    substring(line)
                }
                indexOf = t.indexOf("\n")
            }
        } else {
            curLineText = t
            substring(t)
        }
    }

    private fun checkUpdateView() {
        if (lineText.size == 2) {
            callSuperSetText(lineText[0])
            if (!switcherHandler.isStart) {
                switcherHandler.start()
            }
        }
    }

    private fun substring(line: String) {
        if (measureText(line) > realWidth) {
            substring(line.substring(0, line.length - 1))
        } else {
            val nextLineText = curLineText!!.substring(line.length)
            lineText.add(line)
            checkUpdateView()
            if (!TextUtils.isEmpty(nextLineText) && measureText(nextLineText) < realWidth) {
                lineText.add(nextLineText)
                checkUpdateView()
            } else {
                curLineText = nextLineText
                substring(nextLineText)
            }
        }
    }

    private fun measureText(text: String): Float {
        return (currentView as TextView).paint.measureText(text.replace("\n", ""))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        realWidth = w - paddingLeft - paddingRight
        if (needMeasureText && text != null) {
            onMeasureText(text!!.toString())
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val parentLeft = paddingLeft
        val parentRight = right - parentLeft - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - parentTop - paddingBottom
        getChildAt(0).layout(parentLeft, parentTop, parentRight, parentBottom)
        getChildAt(1).layout(parentLeft, parentTop, parentRight, parentBottom)
    }

    override fun makeView(): View {
        val tv = TextView(context)
        tv.gravity = gravity
        tv.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        tv.maxLines = 1
        tv.setPadding(0, 0, 0, 0)
        tv.setTextColor(textColor)
        tv.paint.textSize = textSize
        return tv
    }

    fun stop() {
        switcherHandler.stop()
    }

    @SuppressLint("HandlerLeak")
    private inner class SwitcherHandler internal constructor() : Handler(Looper.getMainLooper()) {
        internal var isStart = false
            private set

        private var curRunnable: SwitcherRunnable? = null

        internal fun start() {
            isStart = true
            if (curRunnable != null) {
                removeCallbacks(curRunnable!!.stop(), null)
            }
            curRunnable = SwitcherRunnable()
            postDelayed(curRunnable, intervalDuration.toLong())
        }

        internal fun stop() {
            isStart = false
            if (curRunnable != null) {
                removeCallbacks(curRunnable!!.stop(), null)
            }
        }
    }

    private inner class SwitcherRunnable : Runnable {

        private var isFinish = false

        override fun run() {
            if (!isFinish) {
                callSuperSetText(lineText[++curIndex % lineText.size])
                switcherHandler.start()
            }
        }

        internal fun stop(): Runnable {
            isFinish = true
            return this
        }
    }
}