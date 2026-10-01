package com.simprints.face.capture.screens.livefeedback.views

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

/**
 * Paints capture progress along the contour of the square tracking a face, so the progress reads
 * as that square filling in rather than as a separate widget.
 *
 * The cutout capture keeps its own [ProgressPainter], which draws an arc on a fixed circle. This
 * one exists because a square that moves with the face needs a path it can trace, not an arc.
 *
 * The contour starts at the top centre and runs clockwise.
 */
@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class TrackedProgressPainter(
    progressColor: Int,
    strokeWidth: Float,
    dashWidth: Float,
    dashSpace: Float,
) {
    private val contour = Path()
    private val progressPath = Path()
    private val pathMeasure = PathMeasure()
    private val bounds = RectF()
    private val corner = RectF()

    private var cornerRadius = 0f
    private var contourLength = 0f
    private var value = 0f
    private var isProgressPathStale = true

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.strokeWidth = strokeWidth
        color = progressColor
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashSpace), dashSpace)
    }

    var progressColor = progressColor
        set(value) {
            field = value
            progressPaint.color = value
        }

    fun draw(canvas: Canvas) {
        if (contourLength <= 0f || value <= 0f) return
        if (isProgressPathStale) rebuildProgressPath()
        canvas.drawPath(progressPath, progressPaint)
    }

    /** Positions the outline. [rect] is expected to be square, but any rect is handled. */
    fun setContour(
        rect: RectF,
        cornerRadius: Float,
    ) {
        if (bounds == rect && this.cornerRadius == cornerRadius) return
        bounds.set(rect)
        this.cornerRadius = cornerRadius
        rebuildContour()
        isProgressPathStale = true
    }

    fun setValue(value: Float) {
        val coerced = value.coerceIn(0f, 1f)
        if (this.value == coerced) return
        this.value = coerced
        isProgressPathStale = true
    }

    private fun rebuildContour() {
        contour.rewind()
        contourLength = 0f
        if (bounds.isEmpty) return

        val radius = cornerRadius.coerceAtMost(minOf(bounds.width(), bounds.height()) / 2f)
        val diameter = radius * 2f
        with(contour) {
            moveTo(bounds.centerX(), bounds.top)
            lineTo(bounds.right - radius, bounds.top)
            corner.set(bounds.right - diameter, bounds.top, bounds.right, bounds.top + diameter)
            arcTo(corner, TOP, QUARTER_TURN, false)
            lineTo(bounds.right, bounds.bottom - radius)
            corner.set(bounds.right - diameter, bounds.bottom - diameter, bounds.right, bounds.bottom)
            arcTo(corner, RIGHT, QUARTER_TURN, false)
            lineTo(bounds.left + radius, bounds.bottom)
            corner.set(bounds.left, bounds.bottom - diameter, bounds.left + diameter, bounds.bottom)
            arcTo(corner, BOTTOM, QUARTER_TURN, false)
            lineTo(bounds.left, bounds.top + radius)
            corner.set(bounds.left, bounds.top, bounds.left + diameter, bounds.top + diameter)
            arcTo(corner, LEFT, QUARTER_TURN, false)
            lineTo(bounds.centerX(), bounds.top)
        }

        pathMeasure.setPath(contour, false)
        contourLength = pathMeasure.length
    }

    private fun rebuildProgressPath() {
        progressPath.rewind()
        pathMeasure.getSegment(0f, contourLength * value, progressPath, true)
        isProgressPathStale = false
    }

    companion object {
        private const val QUARTER_TURN = 90f
        private const val TOP = 270f
        private const val RIGHT = 0f
        private const val BOTTOM = 90f
        private const val LEFT = 180f
    }
}
