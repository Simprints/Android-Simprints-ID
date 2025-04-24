package com.simprints.feature.externalcredential.screens.ocr.view

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.PointF
import android.widget.ImageView

class ZoomController(private val imageView: ImageView, private val zoomFactor: Float) {

    var isZoomed = false
        private set
    private val zoomMatrix = Matrix()

    fun toggleZoom(onComplete: (isZoomed: Boolean) -> Unit) {
        val startMatrix = Matrix(imageView.imageMatrix)
        val endMatrix = if (isZoomed) {
            fitCenterMatrix()
        } else {
            val baseMatrix = fitCenterMatrix()
            val center = getImageDisplayCenter(baseMatrix)
            Matrix(baseMatrix).apply {
                postScale(zoomFactor, zoomFactor, center.x, center.y)
            }
        }

        imageView.scaleType = ImageView.ScaleType.MATRIX
        animateMatrixTransition(startMatrix, endMatrix)

        if (isZoomed) {
            imageView.setOnTouchListener(null)
        } else {
            imageView.setOnTouchListener(TouchZoomDragListener(imageView, endMatrix))
        }

        zoomMatrix.set(endMatrix)
        isZoomed = !isZoomed
        onComplete(isZoomed)
    }

    private fun getImageDisplayCenter(baseMatrix: Matrix): PointF {
        val drawable = imageView.drawable ?: return PointF(imageView.width / 2f, imageView.height / 2f)

        val values = FloatArray(9)
        baseMatrix.getValues(values)

        val imageWidth = drawable.intrinsicWidth * values[Matrix.MSCALE_X]
        val imageHeight = drawable.intrinsicHeight * values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val centerX = transX + imageWidth / 2f
        val centerY = transY + imageHeight / 2f
        return PointF(centerX, centerY)
    }

    private fun fitCenterMatrix(): Matrix {
        val drawable = imageView.drawable ?: return Matrix()

        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
        val dx = (viewWidth - imageWidth * scale) / 2f
        val dy = (viewHeight - imageHeight * scale) / 2f

        return Matrix().apply {
            postScale(scale, scale)
            postTranslate(dx, dy)
        }
    }

    private fun animateMatrixTransition(startMatrix: Matrix, endMatrix: Matrix, duration: Long = 300L) {
        val startValues = FloatArray(9)
        val endValues = FloatArray(9)
        val currentValues = FloatArray(9)

        startMatrix.getValues(startValues)
        endMatrix.getValues(endValues)

        ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                for (i in 0..8) {
                    currentValues[i] = startValues[i] + (endValues[i] - startValues[i]) * fraction
                }
                val interpolatedMatrix = Matrix()
                interpolatedMatrix.setValues(currentValues)
                imageView.imageMatrix = interpolatedMatrix
            }
            start()
        }
    }
}
