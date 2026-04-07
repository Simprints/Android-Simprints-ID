package com.simprints.infra.view.imagecapture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.simprints.infra.uibase.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI Code")
class CaptureMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    var maskColor: Int
        @ColorInt get() = bgPaint.color
        set(value) {
            bgPaint.color = value
            invalidate()
        }

    private val bgPaint = Paint()
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private val targetWindowLocation = IntArray(2)
    private val selfWindowLocation = IntArray(2) // This view's top-left corner in screen coordinates

    private var targetViewId: Int = NO_ID
    private var resolvedTarget: CaptureTargetView? = null

    // Reusable objects for drawing
    private val cutoutPath = Path()
    private val transformMatrix = Matrix()

    init {
        // Required for PorterDuff.Mode.CLEAR to work correctly
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        context.theme
            .obtainStyledAttributes(attrs, R.styleable.CaptureMaskView, 0, 0)
            .apply {
                try {
                    targetViewId = getResourceId(R.styleable.CaptureMaskView_targetViewId, NO_ID)

                    bgPaint.color = getColor(
                        R.styleable.CaptureMaskView_maskColor,
                        ContextCompat.getColor(context, IDR.color.simprints_black),
                    )
                } finally {
                    recycle()
                }
            }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val target = resolveTarget()

        // In edit mode, retry every frame until the hierarchy is ready
        // Without this check the XML preview is laggy and impossible to work with
        if (target == null) {
            if (!isInEditMode) return
            invalidate()
            return
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        getLocationInWindow(selfWindowLocation)
        target.getLocationInWindow(targetWindowLocation)

        val offsetX = (targetWindowLocation[0] - selfWindowLocation[0]).toFloat()
        val offsetY = (targetWindowLocation[1] - selfWindowLocation[1]).toFloat()

        drawCaptureTargetCutout(canvas, target, offsetX, offsetY)
    }

    private fun resolveTarget(): CaptureTargetView? {
        if (targetViewId == NO_ID) return null
        val current = resolvedTarget
        // Making sure we're not referencing a detached view
        if (current != null && current.isAttachedToWindow) return current
        return rootView?.findViewById<CaptureTargetView>(targetViewId)?.also {
            resolvedTarget = it
        }
    }

    private fun drawCaptureTargetCutout(
        canvas: Canvas,
        target: CaptureTargetView,
        offsetX: Float,
        offsetY: Float,
    ) {
        transformMatrix.setTranslate(offsetX, offsetY)
        cutoutPath.rewind()
        cutoutPath.addPath(target.shapePath, transformMatrix)
        canvas.drawPath(cutoutPath, clearPaint)
    }
}
