package com.simprints.feature.externalcredential.screens.ocr.view

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

class TouchZoomDragListener(
    private val imageView: ImageView,
    private val matrix: Matrix
) : View.OnTouchListener {

    private val startPoint = PointF()
    private var mode = NONE

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startPoint.set(event.x, event.y)
                mode = DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val dx = event.x - startPoint.x
                    val dy = event.y - startPoint.y

                    val clamped = clampTranslation(dx, dy)
                    matrix.postTranslate(clamped.first, clamped.second)
                    imageView.imageMatrix = matrix

                    startPoint.set(event.x, event.y) // 🔁 update for next delta
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                mode = NONE
                v?.performClick()
            }
        }
        return true
    }

    private fun clampTranslation(dx: Float, dy: Float): Pair<Float, Float> {
        val drawable = imageView.drawable ?: return dx to dy

        val values = FloatArray(9)
        matrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val scaledWidth = drawable.intrinsicWidth * scaleX
        val scaledHeight = drawable.intrinsicHeight * scaleY
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()

        val minTransX = viewWidth - scaledWidth
        val minTransY = viewHeight - scaledHeight

        val newTransX = (transX + dx).coerceIn(minTransX, 0f)
        val newTransY = (transY + dy).coerceIn(minTransY, 0f)

        return newTransX - transX to newTransY - transY
    }
}

