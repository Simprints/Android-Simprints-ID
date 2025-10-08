package com.simprints.feature.externalcredential.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.simprints.feature.externalcredential.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI Code")
class DocumentScanMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val bgPaint = Paint()
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private val rect = Rect()
    private var targetViewId: Int = NO_ID
    private var cornerRadius: Float? = null
    private var insetValue: Float? = null

    init {
        context.theme
            .obtainStyledAttributes(
                attrs,
                R.styleable.DocumentMaskView,
                0,
                0,
            ).apply {
                try {
                    targetViewId = getResourceId(R.styleable.DocumentMaskView_targetViewId, NO_ID)

                    val backgroundColor = getColor(
                        R.styleable.DocumentMaskView_maskColor,
                        ContextCompat.getColor(context, IDR.color.simprints_black),
                    )
                    bgPaint.color = backgroundColor

                    if (hasValue(R.styleable.DocumentMaskView_cornerRadius)) {
                        cornerRadius = getDimension(R.styleable.DocumentMaskView_cornerRadius, 0f)
                    }

                    if (hasValue(R.styleable.DocumentMaskView_inset)) {
                        insetValue = getDimension(R.styleable.DocumentMaskView_inset, 0f)
                    }
                } finally {
                    recycle()
                }
            }

        require(targetViewId != NO_ID) { "targetViewId must be specified" }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (parent as ViewGroup).findViewById<View>(targetViewId)?.let { targetView ->
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Calculating target view position relative to this view
            rect.setEmpty()
            targetView.getGlobalVisibleRect(rect)
            val offset = IntArray(2)
            getLocationOnScreen(offset)
            rect.offset(-offset[0], -offset[1])

            // Apply inset if specified. This might be useful for QR scanners to create sense of depth
            insetValue?.let { inset ->
                val insetInt = inset.toInt()
                rect.inset(insetInt, insetInt)
            }

            // Cutting out central area. Rounded or rectangular, based on cornerRadius
            val radius = cornerRadius
            if (radius != null && radius > 0f) {
                canvas.drawRoundRect(
                    rect.left.toFloat(),
                    rect.top.toFloat(),
                    rect.right.toFloat(),
                    rect.bottom.toFloat(),
                    radius,
                    radius,
                    clearPaint,
                )
            } else {
                canvas.drawRect(rect, clearPaint)
            }
        }
    }
}
