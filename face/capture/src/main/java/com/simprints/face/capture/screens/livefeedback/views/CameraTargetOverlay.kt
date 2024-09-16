package com.simprints.face.capture.screens.livefeedback.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.simprints.core.tools.extentions.dpToPx
import com.simprints.face.capture.models.ScreenOrientation
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class CameraTargetOverlay(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageView(context, attrs) {
    companion object {
        private val SEMI_TRANSPARENT_OVERLAY = Color.argb(102, 0, 0, 0)
        private val WHITE_OVERLAY = Color.argb(242, 255, 255, 255)

        /**
         * Reference to the guideline's percentage of margin from the top of the screen. Used when
         * the screen is in the portrait (vertical) mode
         */
        private const val percentFromTopPortrait = 0.3f

        /**
         * Reference to the guideline's percentage of margin from the top of the screen. Used when
         * the screen is in the landscape (horizontal) mode
         */
        private const val percentFromTopLandscape = 0.5f

        fun rectForPlane(
            width: Int,
            height: Int,
            rectSize: Float,
            screenOrientation: ScreenOrientation
        ): RectF {
            val percentFromTop = when (screenOrientation) {
                ScreenOrientation.Landscape -> percentFromTopLandscape
                ScreenOrientation.Portrait -> percentFromTopPortrait
            }
            val top = (height * percentFromTop) - (rectSize / 2)
            val bottom = top + rectSize

            val centerWidth = width / 2
            val left = centerWidth - (rectSize / 2)
            val right = left + rectSize

            return RectF(left, top, right, bottom)
        }
    }

    private var drawingFunc: (Canvas.() -> Unit)? = null
        set(value) {
            field = value
            postInvalidate()
        }

    private val rectSize = 240f.dpToPx(context)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val circleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f.dpToPx(context)
        color = Color.argb(80, 255, 255, 255)
        strokeCap = Paint.Cap.ROUND
    }
    var rectInCanvas = RectF(0f, 0f, 0f, 0f)

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawingFunc?.invoke(canvas)
    }

    fun drawSemiTransparentTarget(screenOrientation: ScreenOrientation) {
        drawingFunc = {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawColor(SEMI_TRANSPARENT_OVERLAY, PorterDuff.Mode.SRC_OVER)
            drawTarget(screenOrientation)
        }
    }

    fun drawWhiteTarget(screenOrientation: ScreenOrientation) {
        drawingFunc = {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawColor(WHITE_OVERLAY, PorterDuff.Mode.SRC_OVER)
            drawTarget(screenOrientation)
        }
    }

    private fun Canvas.drawTarget(screenOrientation: ScreenOrientation) {
        rectInCanvas = rectForPlane(width, height, rectSize, screenOrientation)

        drawOval(rectInCanvas, circlePaint)
        drawOval(rectInCanvas, circleBorderPaint)
    }
}
