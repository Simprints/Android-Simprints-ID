package com.simprints.feature.externalcredential.screens.ocr.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.google.mlkit.vision.text.Text

class OcrBoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var lines: List<Text.Line> = emptyList()
    private var imageView: ImageView? = null

    fun init(lines: List<Text.Line>, imageView: ImageView) {
        this.lines = lines
        this.imageView = imageView
        invalidate()
    }

    private val paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (line in lines) {
            line.cornerPoints?.let { ocrPoints ->
                val scaledPoints = ocrPoints.map { mapPointToImageView(it) }
                val points = scaledPoints.map { Point(it.x.toInt(), it.y.toInt()) }.toTypedArray()
                if (points.size == 4) {
                    val path = Path().apply {
                        moveTo(points[0].x.toFloat(), points[0].y.toFloat())
                        lineTo(points[1].x.toFloat(), points[1].y.toFloat())
                        lineTo(points[2].x.toFloat(), points[2].y.toFloat())
                        lineTo(points[3].x.toFloat(), points[3].y.toFloat())
                        close()
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
    }
    private fun mapPointToImageView(point: Point): PointF {
        val imageMatrix = Matrix()
        imageView?.imageMatrix?.let { imageMatrix.set(it) }

        val points = floatArrayOf(point.x.toFloat(), point.y.toFloat())
        imageMatrix.mapPoints(points)

        return PointF(points[0], points[1])
    }

}

