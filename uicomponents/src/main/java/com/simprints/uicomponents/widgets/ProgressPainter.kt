package com.simprints.uicomponents.widgets

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF

/**
 * This is the brawl that paints the progress bar to the screen.
 *
 * [startAngle] is 270 meaning that the it starts on the top of the screen.
 *
 * Every time [setValue] is called, [plusAngle] is changed. On the next [draw], this Painter will
 * created dashed lines up the angle defined.
 */
class ProgressPainter(
    progressColor: Int,
    var max: Int,
    private val strokeWidth: Float,
    private val dashWidth: Float,
    private val dashSpace: Float
) {
    private var progressCircle: RectF = RectF()
    private lateinit var progressPaint: Paint
    private val startAngle = 270f
    private var plusAngle = 0f
    private var width: Int = 0
    private var height: Int = 0
    var progressColor = progressColor
        set(value) {
            field = value
            progressPaint.color = value
        }

    init {
        initCirclePainter()
    }

    private fun initCirclePainter() {
        progressPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = this@ProgressPainter.strokeWidth
            color = progressColor
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashSpace), dashSpace)
        }
    }

    fun draw(canvas: Canvas?) {
        canvas?.drawArc(progressCircle, startAngle, plusAngle, false, progressPaint)
    }

    fun onSizeChanged(height: Int, width: Int) {
        this.height = height
        this.width = width
        changeInternalCircleSize()
    }

    private fun changeInternalCircleSize() {
        val padding = strokeWidth / 2
        progressCircle.set(
            0f - padding,
            0f - padding,
            width.toFloat() + padding,
            height.toFloat() + padding
        )
    }

    fun setValue(value: Float) {
        plusAngle = (360 * value) / max
    }
}
