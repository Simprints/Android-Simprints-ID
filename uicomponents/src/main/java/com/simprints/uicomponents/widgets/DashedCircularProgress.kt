package com.simprints.uicomponents.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.simprints.uicomponents.R
import com.simprints.core.tools.extentions.dpToPx

/**
 * This is a variant of circular progress bar that have spaces between bars (as opposed to a filled
 * progress bar).
 *
 * How to use:
 * - Add it to you xml as a normal view
 * - (optional) You can change the color of the progress and the max number of the steps by using
 * app:dcp_color and app:dcp_max.
 * - Update the "fill" of the bar by setting a new [value] in your code
 *
 * You can also set the number of steps as the [max] property. This doesn't translate to number of bars, it's
 * just the max number of steps you are going to use.
 */
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
            12f.dpToPx(context),
            3f.dpToPx(context),
            1f.dpToPx(context)
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
