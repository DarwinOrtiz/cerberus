package com.salmoukas.cerberus.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.salmoukas.cerberus.util.TimeRange
import com.salmoukas.cerberus.util.TimeRangeWithCheckStatus
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min


class CheckTimelineView : View {

    companion object {
        const val COLOR_UNKNOWN = Color.LTGRAY
        const val COLOR_OK = Color.GREEN
        const val COLOR_ERROR = Color.RED
    }

    data class ViewModel(
        val range: TimeRange,
        val results: List<TimeRangeWithCheckStatus>
    )

    var viewModel: ViewModel? = null
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // measure height
        val desiredHeight = 100
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                min(desiredHeight, heightSize)
            }
            else -> {
                desiredHeight
            }
        }

        // set dimensions
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_UNKNOWN
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || viewModel == null) {
            return
        }

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            paintBg.apply { color = COLOR_UNKNOWN })

        val xTransform =
            { x: Long -> width - (x - viewModel!!.range.begin) * width / (viewModel!!.range.end - viewModel!!.range.begin).toFloat() }

        viewModel!!.results.onEach {
            canvas.drawRect(
                floor(xTransform(it.end)),
                0f,
                ceil(xTransform(it.begin)),
                height.toFloat(),
                paintBg.apply { color = if (it.ok) COLOR_OK else COLOR_ERROR }
            )
        }
    }
}
