package com.simprints.feature.externalcredential.screens.ocr.view
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class IdCardMaskView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var cutoutRect: RectF? = null
        set(value) {
            field = value
            invalidate()
        }

    var horizontalPaddingDp: Float = 16f

    private val backgroundPaint = Paint().apply {
        color = "#88000000".toColorInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rect = cutoutRect ?: return
        val path = Path().apply {
            addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            addRoundRect(rect, horizontalPaddingDp, horizontalPaddingDp, Path.Direction.CCW)
        }

        canvas.drawPath(path, backgroundPaint)
    }
}
