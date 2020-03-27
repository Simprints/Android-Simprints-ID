package com.simprints.face.capture.livefeedback.tools

import android.graphics.*
import android.view.SurfaceView
import com.simprints.face.detection.Face
import com.simprints.uicomponents.extensions.scaled
import com.simprints.uicomponents.extensions.shifted


class CanvasDrawer(private val canvas: SurfaceView?) {

    companion object {
        // The target bounding box for the end user's face, as percents of surface view
        // e.g: RectF(0.1F, 0.1F, 0.9F, 0.6F) means:
        // - 80% (= 0.9 - 0.1) of the width of the view, centered
        // - 50% (= 0.6 - 0.1) of the height of the view, starting 10% from the top
        val relativeTargetBoundingBoxOnDisplay = RectF(0.16F, 0.12F, 0.84F, 0.48F)

        const val LANDMARK_CIRCLE_RADIUS = 14F

        private val POSITIVE_TARGET_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.GREEN
            strokeWidth = 14F
        }

        private val NEGATIVE_TARGET_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.BLUE
            strokeWidth = 14F
        }

        private val POSITIVE_FACE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.GREEN
            strokeWidth = 5F
        }

        private val NEGATIVE_FACE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.BLUE
            strokeWidth = 5F
        }
    }

    data class CanvasUI(val face: Face?, val valid: Boolean)

    init {
        canvas?.setZOrderOnTop(true)
        canvas?.holder?.setFormat(PixelFormat.TRANSLUCENT)
    }

    fun drawFaceTrackingUI(canvasUI: CanvasUI) {
        canvas?.holder?.lockCanvas()?.let { canvas ->
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawTarget(canvasUI.valid)
            if (canvasUI.face != null) {
                canvas.drawFace(canvasUI.face, canvasUI.valid)
            }
            this.canvas.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun Canvas.drawTarget(positive: Boolean) {
        val paint = if (positive) {
            POSITIVE_TARGET_PAINT
        } else {
            NEGATIVE_TARGET_PAINT
        }
        val rectInCanvas = relativeTargetBoundingBoxOnDisplay.scaled(width, height)
        drawOval(rectInCanvas, paint)
    }

    private fun Canvas.drawFace(face: Face, positive: Boolean) {
        val paint = if (positive) {
            POSITIVE_FACE_PAINT
        } else {
            NEGATIVE_FACE_PAINT
        }
        val targetInCanvas = relativeTargetBoundingBoxOnDisplay.scaled(width, height)
        val rectInTarget =
            face.relativeBoundingBox.scaled(targetInCanvas.width(), targetInCanvas.height())
        val rectInCanvas = rectInTarget.shifted(targetInCanvas.left, targetInCanvas.top)
        drawOval(rectInCanvas, paint)
    }


}
