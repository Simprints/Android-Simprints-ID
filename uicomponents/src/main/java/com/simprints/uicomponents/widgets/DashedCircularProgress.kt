package com.simprints.uicomponents.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import com.simprints.uicomponents.R

class DashedCircularProgress(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var progressPainter: ProgressPainter? = null
    var progressColor = Color.WHITE
        set(value) {
            field = value
            progressPainter?.progressColor = value
        }
    var max = 100
        set(value) {
            field = value
            progressPainter?.max = value
            invalidate()
        }
    var value = 0f
        set(value) {
            field = value
            progressPainter?.setValue(value)
            invalidate()
        }

    init {
        setWillNotDraw(false)
        initAttributes(context.obtainStyledAttributes(attrs, R.styleable.DashedCircularProgress))
        initPainters()
    }

    private fun initAttributes(attributes: TypedArray) {
        progressColor =
            attributes.getColor(R.styleable.DashedCircularProgress_dcp_color, progressColor)
        max = attributes.getInt(R.styleable.DashedCircularProgress_dcp_max, max)
    }

    private fun initPainters() {
        progressPainter = ProgressPainter(
            progressColor,
            max,
            // TODO check if core created this extension
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics),
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics),
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        progressPainter?.draw(canvas)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressPainter?.onSizeChanged(h, w)
    }
}