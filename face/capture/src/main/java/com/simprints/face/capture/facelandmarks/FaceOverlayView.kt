package com.simprints.face.capture.facelandmarks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.simprints.infra.logging.Simber

class FaceOverlayView(
    context: Context,
    attrs: AttributeSet?,
) : View(context, attrs) {
    private var face: Face? = null

    private var scaleX = 1.0f
    private var scaleY = 1.0f
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var sourceImageWidth = 0
    private var sourceImageHeight = 0
    private val greenContourPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 10.0f
        alpha = 150
    }
    private var facePainter = greenContourPaint

    private val redContourPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 10.0f
        alpha = 150
    }

    var isFaceInsideTheTarget = false

    /**
     * Call this from your FaceAnalyzer. It will take the first face from the list.
     */
    fun update(
        detectedFace: Face?,
        fullImageWidth: Int,
        fullImageHeight: Int,
        cropRect: RectF,
    ) {
        this.sourceImageWidth = fullImageWidth
        this.sourceImageHeight = fullImageHeight
        this.face = detectedFace
        offsetX = x
        offsetY = y
        this.scaleX = sourceImageWidth / width.toFloat()
        this.scaleY = sourceImageHeight / height.toFloat()
        facePainter = if (face != null && areAllContoursInside(detectedFace!!, cropRect)) {
            isFaceInsideTheTarget = true
            greenContourPaint
        } else {
            isFaceInsideTheTarget = false
            redContourPaint
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val faceToDraw = face ?: return
        Simber.i(
            tag = "FaceOverlayView",
            message = "Drawing points: ${faceToDraw.allContours[0].points[0]} scale: $scaleX, offsetX: $offsetX, offsetY: $offsetY",
        )
        for (contour in faceToDraw.allContours) {
            for (i in 0 until contour.points.size - 1) {
                val startPoint = contour.points[i]

                val endPoint = contour.points[i + 1]

                val startXInFullImage = startPoint.x
                val startYInFullImage = startPoint.y
                val endXInFullImage = endPoint.x
                val endYInFullImage = endPoint.y

                canvas.drawLine(
                    translateX(startXInFullImage),
                    translateY(startYInFullImage),
                    translateX(endXInFullImage),
                    translateY(endYInFullImage),
                    facePainter,
                )
            }
        }
    }

    private fun translateX(x: Float): Float = offsetX + (x / scaleY) - 400 // should be dynamically calculated

    private fun translateY(y: Float): Float = (y / scaleY)

    fun reset() {
        face = null
        invalidate()
    }

    fun areAllContoursInside(
        face: Face,
        rect: RectF,
    ): Boolean {
        val allContours = face.allContours

        for (contour in allContours) {
            for (point in contour.points) {
                if (!rect.contains(translateX(point.x), translateY(point.y))) {
                    return false
                }
            }
        }
        return true
    }
}
