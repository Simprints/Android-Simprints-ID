package com.simprints.face.capture.screens.livefeedback.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.simprints.core.tools.extentions.dpToPx
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.capture.R
import kotlin.math.max
import kotlin.math.min

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class CameraTargetOverlay(
    context: Context, attrs: AttributeSet
) : AppCompatImageView(context, attrs) {
    companion object {
        private val SEMI_TRANSPARENT_OVERLAY = Color.argb(102, 0, 0, 0)
        private val WHITE_OVERLAY = Color.argb(242, 255, 255, 255)
    }

    private var drawingFunc: (Canvas.() -> Unit)? = null
        set(value) {
            field = value
            postInvalidate()
        }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val circleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f.dpToPx(context)
        color = Color.argb(80, 255, 255, 255)
        strokeCap = Paint.Cap.ROUND
    }

    var circleRect = RectF(0f, 0f, 0f, 0f)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawingFunc?.invoke(canvas)
    }

    fun drawSemiTransparentTarget() {
        drawingFunc = {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawColor(SEMI_TRANSPARENT_OVERLAY, PorterDuff.Mode.SRC_OVER)
            drawTarget()
        }
    }

    fun drawWhiteTarget() {
        drawingFunc = {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawColor(WHITE_OVERLAY, PorterDuff.Mode.SRC_OVER)
            drawTarget()
        }
    }

    private fun Canvas.drawTarget() {
        drawOval(circleRect, circlePaint)
        drawOval(circleRect, circleBorderPaint)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Calculate the circle rect only when the view's size changes
        calculateCircleRect(w.toFloat(), h.toFloat())
    }

    private fun calculateCircleRect(width: Float, height: Float) {
        // Calculate the margin as 10% of the max dimension
        val guidelineMarginPercent =
            ResourcesCompat.getFloat(context.resources, R.dimen.guideline_margin_percent)
        val margin = (max(width, height) * guidelineMarginPercent).toInt()

        val multiplier =
            ResourcesCompat.getFloat(context.resources, R.dimen.capture_target_size_percent)

        val radius = (min(width, height) * multiplier) / 2

        // Calculate the center coordinates and radius
        val centerX = width / 2
        val centerY = if (width < height) radius + margin else height / 2

        // Set the dimensions of the circle rect
        circleRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    }


}
