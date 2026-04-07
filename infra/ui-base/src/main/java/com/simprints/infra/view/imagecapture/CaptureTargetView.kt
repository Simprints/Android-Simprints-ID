package com.simprints.infra.view.imagecapture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.simprints.core.tools.extentions.dpToPx
import com.simprints.infra.uibase.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI Code")
class CaptureTargetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    enum class Shape { OVAL, RECT }

    /**
     * Path of the view's shape in local view coordinates
     */
    val shapePath = Path()

    /**
     * XY position of this view's top-left corner in window coordinates
     */
    private val locationInWindow = IntArray(2)

    /**
     * X coordinate representing how far this view is shifted from the window’s origin
     */
    val windowOffsetX: Float
        get() = locationInWindow[0].toFloat()

    /**
     * Y coordinate representing how far this view is shifted from the window’s origin
     */
    val windowOffsetY: Float
        get() = locationInWindow[1].toFloat()

    /**
     * Outer edge of the stroke so that everything can be drawn accurately around it.
     * Android draws strokes centered on the path, so outward edge is half of the [strokeWidth]
     */
    val outerStrokeEdge: Float get() = strokeWidth / 2f

    /**
     * Shape of the target view. See all options in [Shape] enum.
     */
    var shape: Shape = Shape.RECT
        set(value) {
            field = value
            rebuildPath(width, height)
            invalidate()
        }

    /**
     * Corner radius. Used only for [Shape.RECT], ignored for [Shape.OVAL]
     */
    var cornerRadius: Float = 0f
        set(value) {
            field = value
            rebuildPath(width, height)
            invalidate()
        }

    @ColorInt
    var strokeColor: Int = Color.argb(80, 255, 255, 255)
        set(value) {
            field = value
            strokePaint.color = value
            invalidate()
        }

    var strokeWidth: Float = 2f.dpToPx(context)
        set(value) {
            field = value
            strokePaint.strokeWidth = value
            rebuildPath(width, height)
            invalidate()
        }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.CaptureTargetView)
            try {
                shape = Shape.values()[ta.getInt(R.styleable.CaptureTargetView_shape, 0)]
                cornerRadius = ta.getDimension(R.styleable.CaptureTargetView_cornerRadius, 0f)
                strokeColor = ta.getColor(R.styleable.CaptureTargetView_strokeColor, strokeColor)
                strokeWidth = ta.getDimension(R.styleable.CaptureTargetView_strokeWidth, strokeWidth)
            } finally {
                ta.recycle()
            }
        }
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildPath(w, h)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        super.onLayout(changed, left, top, right, bottom)
        getLocationInWindow(locationInWindow)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(shapePath, strokePaint)
    }

    private fun rebuildPath(
        w: Int,
        h: Int,
    ) {
        if (w == 0 || h == 0) return
        val inset = strokeWidth / 2f
        val bounds = RectF(inset, inset, w - inset, h - inset)
        shapePath.rewind()
        when (shape) {
            Shape.OVAL -> shapePath.addOval(bounds, Path.Direction.CW)
            Shape.RECT -> {
                val maxRadius = minOf(bounds.width(), bounds.height()) / 2f
                val r = cornerRadius.coerceAtMost(maxRadius)
                shapePath.addRoundRect(bounds, r, r, Path.Direction.CW)
            }
        }
    }
}
