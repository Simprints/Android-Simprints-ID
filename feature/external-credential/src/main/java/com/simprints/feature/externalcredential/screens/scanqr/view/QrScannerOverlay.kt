package com.simprints.feature.externalcredential.screens.scanqr.view

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
internal class QrScannerOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val bgPaint = Paint().apply {
        color = ContextCompat.getColor(context, IDR.color.simprints_black)
    }
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val rect = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (parent as ViewGroup).findViewById<View>(R.id.qrScannerArea)?.let { qrScannerArea ->
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // geting rect in this view’s coordinates
            rect.setEmpty()
            qrScannerArea.getGlobalVisibleRect(rect)
            val offset = IntArray(2)
            getLocationOnScreen(offset)
            rect.offset(-offset[0], -offset[1])

            // shrink each side so that QR code area indicators and at the top
            val inset = (8 * resources.displayMetrics.density).toInt()
            rect.inset(inset, inset)

            canvas.drawRect(rect, clearPaint)
        }
    }
}
