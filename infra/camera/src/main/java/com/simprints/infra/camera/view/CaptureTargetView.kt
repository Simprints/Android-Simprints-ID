package com.simprints.infra.camera.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.os.BundleCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extensions.dpToPx
import com.simprints.infra.camera.R

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(shapePath, strokePaint)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(BUNDLE_ID_SAVE_INSTANCE_STATE, super.onSaveInstanceState())
        putString(BUNDLE_ID_SHAPE, shape.name)
        putFloat(BUNDLE_ID_CORNER_RADIUS, cornerRadius)
        putInt(BUNDLE_ID_STROKE_COLOR, strokeColor)
        putFloat(BUNDLE_ID_STROKE_WIDTH, strokeWidth)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            shape = Shape.entries.firstOrNull { it.name == state.getString(BUNDLE_ID_SHAPE) } ?: shape
            cornerRadius = state.getFloat(BUNDLE_ID_CORNER_RADIUS, cornerRadius)
            strokeColor = state.getInt(BUNDLE_ID_STROKE_COLOR, strokeColor)
            strokeWidth = state.getFloat(BUNDLE_ID_STROKE_WIDTH, strokeWidth)
            super.onRestoreInstanceState(BundleCompat.getParcelable(state, BUNDLE_ID_SAVE_INSTANCE_STATE, BaseSavedState::class.java))
        } else {
            super.onRestoreInstanceState(state)
        }
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

    companion object {
        private const val BUNDLE_ID_SAVE_INSTANCE_STATE = "BUNDLE_ID_SAVE_INSTANCE_STATE"
        private const val BUNDLE_ID_SHAPE = "BUNDLE_ID_SHAPE"
        private const val BUNDLE_ID_CORNER_RADIUS = "BUNDLE_ID_CORNER_RADIUS"
        private const val BUNDLE_ID_STROKE_COLOR = "BUNDLE_ID_STROKE_COLOR"
        private const val BUNDLE_ID_STROKE_WIDTH = "BUNDLE_ID_STROKE_WIDTH"
    }
}
