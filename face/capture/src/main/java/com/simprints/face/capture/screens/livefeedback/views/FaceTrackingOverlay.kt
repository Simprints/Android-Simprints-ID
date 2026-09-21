package com.simprints.face.capture.screens.livefeedback.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.simprints.core.tools.extensions.dpToPx
import com.simprints.face.capture.screens.livefeedback.FaceTargetBox
import com.simprints.face.capture.screens.livefeedback.Progress
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import com.simprints.infra.resources.R as IDR

/**
 * Draws the live face-tracking feedback over the whole camera preview: a colour-coded square
 * around the detected face, plus the capture progress traced along that square's contour so it
 * follows the face instead of a fixed screen cutout.
 *
 * Nothing is dimmed - the preview stays fully visible and the square colour carries the state.
 *
 * ### Smoothing
 * Detections only arrive as fast as the SDK can analyse a frame, which is far slower than the
 * display refreshes, so drawing each one directly makes the square step from position to position.
 * Instead the square glides toward the latest detection, redrawing itself between detections until
 * it catches up. How hard it is smoothed depends on how far behind it is, so that a still face
 * gets a calm square and a moving one gets a square that stays on it - see [timeConstantFor].
 *
 * This is presentation only. The view never reports geometry back, so the crop handed to the rest
 * of the pipeline still comes from the unsmoothed detection and never lags behind the face. The
 * tint is likewise applied immediately rather than glided, so colour never describes a position
 * the face has already left.
 */
@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class FaceTrackingOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val boxStrokeWidth = 3f.dpToPx(context)
    private val boxCornerRadius = 8f.dpToPx(context)
    private val progressStrokeWidth = 8f.dpToPx(context)

    /** Clears the box outline so the two strokes sit side by side instead of overlapping. */
    private val progressOutset = (boxStrokeWidth + progressStrokeWidth) / 2f + 4f.dpToPx(context)

    /** Below this the remaining distance is not worth another frame, so the glide ends. */
    private val settleThreshold = 0.5f.dpToPx(context)

    /** Up to here the square is assumed to be chasing detector noise rather than a moving face. */
    private val jitterDistance = 2f.dpToPx(context)

    /** From here on the face is genuinely moving and the square should keep up with it. */
    private val motionDistance = 16f.dpToPx(context)

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = boxStrokeWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val progressPainter = TrackedProgressPainter(
        progressColor = Color.WHITE,
        strokeWidth = progressStrokeWidth,
        dashWidth = 3f.dpToPx(context),
        dashSpace = 1f.dpToPx(context),
    )

    private val aimGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f.dpToPx(context)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = AIM_GUIDE_COLOR
    }

    /** Corner brackets marking where the subject's face should go. Rebuilt only on resize. */
    private val aimGuidePath = Path()

    /** Latest detection in view pixels: where the square is heading. */
    private val targetRect = RectF()

    /** Where the square actually is this frame. */
    private val displayedRect = RectF()
    private var hasDisplayedRect = false
    private var lastDrawTimeMs = 0L

    private val drawRect = RectF()
    private val progressBounds = RectF()

    private val invalidColor by lazy { color(IDR.color.simprints_red) }
    private val warningColor by lazy { color(IDR.color.simprints_yellow) }
    private val validColor by lazy { color(IDR.color.simprints_green_light) }
    private val defaultProgressColor by lazy { color(IDR.color.simprints_blue_grey_light) }
    private val validProgressColor by lazy { color(IDR.color.simprints_green_light) }
    private val validationProgressColor by lazy { color(IDR.color.simprints_orange) }

    private var target: FaceTargetBox? = null
    private var progress: Progress = Progress.HIDDEN
    private var showAimGuide: Boolean = false

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    /**
     * Renders one frame of feedback. A null [target] means no face is currently tracked.
     *
     * [showAimGuide] draws the corner brackets that tell the operator where to put the subject.
     */
    fun update(
        target: FaceTargetBox?,
        progress: Progress,
        showAimGuide: Boolean,
    ) {
        if (this.target == target && this.progress == progress && this.showAimGuide == showAimGuide) return
        this.showAimGuide = showAimGuide

        if (target == null) {
            // Nothing to glide from once the face is gone; the next one appears where it is
            hasDisplayedRect = false
        } else if (this.target?.rect != target.rect) {
            // Measure the first step of the glide from now rather than from the last frame drawn,
            // which may have been a whole detection ago
            lastDrawTimeMs = AnimationUtils.currentAnimationTimeMillis()
        }

        this.target = target
        this.progress = progress
        invalidate()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        // View pixels mean something different now, so start again rather than glide across
        hasDisplayedRect = false
        rebuildAimGuide()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        if (showAimGuide) canvas.drawPath(aimGuidePath, aimGuidePaint)

        val box = target
        val isGliding = box != null && advanceTowards(box.rect)

        if (box != null) {
            boxPaint.color = colorFor(box.tint)
            drawRect.set(displayedRect)
            // Inset so the outline is never half-clipped against the edge of the preview
            drawRect.inset(boxStrokeWidth / 2f, boxStrokeWidth / 2f)
            canvas.drawRoundRect(drawRect, boxCornerRadius, boxCornerRadius, boxPaint)
        }

        if (progress.visible) {
            progressPainter.progressColor = colorFor(progress.tint)
            progressPainter.setValue(progress.value)
            progressPainter.setContour(
                progressContourAround(displayedRect.takeIf { box != null }),
                boxCornerRadius + progressOutset,
            )
            progressPainter.draw(canvas)
        }

        if (isGliding) postInvalidateOnAnimation()
    }

    /**
     * Lays out the aim guide: four corner brackets around a centred square, which read as "put the
     * face here" without walling off the rest of the preview the way a solid frame would. The
     * guide is advisory - a face outside it is still captured if it is clearly the dominant one.
     */
    private fun rebuildAimGuide() {
        aimGuidePath.rewind()
        if (width == 0 || height == 0) return

        val half = min(width, height) * AIM_GUIDE_SIZE_RATIO / 2f
        val left = width / 2f - half
        val top = height / 2f - half
        val right = width / 2f + half
        val bottom = height / 2f + half
        val arm = half * 2f * AIM_GUIDE_ARM_RATIO

        with(aimGuidePath) {
            moveTo(left, top + arm)
            lineTo(left, top)
            lineTo(left + arm, top)

            moveTo(right - arm, top)
            lineTo(right, top)
            lineTo(right, top + arm)

            moveTo(right, bottom - arm)
            lineTo(right, bottom)
            lineTo(right - arm, bottom)

            moveTo(left + arm, bottom)
            lineTo(left, bottom)
            lineTo(left, bottom - arm)
        }
    }

    /**
     * Moves [displayedRect] one time-proportional step toward the normalised [normalisedTarget].
     *
     * @return true while the square is still short of its target and needs another frame
     */
    private fun advanceTowards(normalisedTarget: RectF): Boolean {
        targetRect.set(
            normalisedTarget.left * width,
            normalisedTarget.top * height,
            normalisedTarget.right * width,
            normalisedTarget.bottom * height,
        )

        if (!hasDisplayedRect) {
            displayedRect.set(targetRect)
            hasDisplayedRect = true
            lastDrawTimeMs = AnimationUtils.currentAnimationTimeMillis()
            return false
        }

        val distance = distanceToTarget()
        if (distance < settleThreshold) {
            displayedRect.set(targetRect)
            lastDrawTimeMs = AnimationUtils.currentAnimationTimeMillis()
            return false
        }

        val now = AnimationUtils.currentAnimationTimeMillis()
        // Driven by elapsed time so the glide takes the same wall-clock time on any refresh rate.
        // Capped so a spell with no frames drawn resolves over a few frames instead of snapping.
        val deltaMs = (now - lastDrawTimeMs).coerceIn(0L, MAX_FRAME_DELTA_MS)
        lastDrawTimeMs = now

        val progressTowardsTarget = 1f - exp(-deltaMs.toFloat() / timeConstantFor(distance))
        displayedRect.set(
            lerp(displayedRect.left, targetRect.left, progressTowardsTarget),
            lerp(displayedRect.top, targetRect.top, progressTowardsTarget),
            lerp(displayedRect.right, targetRect.right, progressTowardsTarget),
            lerp(displayedRect.bottom, targetRect.bottom, progressTowardsTarget),
        )

        return true
    }

    /** How far the drawn square still has to travel, as its furthest-off edge. */
    private fun distanceToTarget(): Float = maxOf(
        abs(displayedRect.left - targetRect.left),
        abs(displayedRect.top - targetRect.top),
        abs(displayedRect.right - targetRect.right),
        abs(displayedRect.bottom - targetRect.bottom),
    )

    /**
     * Picks how hard to smooth, based on how far behind the square currently is.
     */
    private fun timeConstantFor(distance: Float): Float {
        val towardsMotion = ((distance - jitterDistance) / (motionDistance - jitterDistance)).coerceIn(0f, 1f)
        return lerp(GLIDE_TIME_CONSTANT_STILL_MS, GLIDE_TIME_CONSTANT_MOVING_MS, towardsMotion)
    }

    private fun lerp(
        from: Float,
        to: Float,
        fraction: Float,
    ) = from + (to - from) * fraction

    /**
     * The progress traces the square itself, just outside its outline. Without a tracked face it
     * falls back to a centred square so progress stays visible between detections.
     */
    private fun progressContourAround(box: RectF?): RectF {
        if (box == null) {
            val half = min(width, height) * FALLBACK_PROGRESS_SIZE_RATIO / 2f
            progressBounds.set(width / 2f - half, height / 2f - half, width / 2f + half, height / 2f + half)
        } else {
            progressBounds.set(box)
            progressBounds.inset(-progressOutset, -progressOutset)
        }
        return progressBounds
    }

    @ColorInt
    private fun colorFor(tint: FaceTargetBox.Tint): Int = when (tint) {
        FaceTargetBox.Tint.INVALID -> invalidColor
        FaceTargetBox.Tint.WARNING -> warningColor
        FaceTargetBox.Tint.VALID -> validColor
    }

    @ColorInt
    private fun colorFor(tint: Progress.Tint): Int = when (tint) {
        Progress.Tint.DEFAULT -> defaultProgressColor
        Progress.Tint.VALID -> validProgressColor
        Progress.Tint.VALIDATION -> validationProgressColor
    }

    @ColorInt
    private fun color(resId: Int) = ContextCompat.getColor(context, resId)

    companion object {
        private const val FALLBACK_PROGRESS_SIZE_RATIO = 0.7f

        /** Side of the aim guide as a fraction of the preview's shorter edge. */
        private const val AIM_GUIDE_SIZE_RATIO = 0.62f

        /** Length of each bracket arm as a fraction of the guide's side. */
        private const val AIM_GUIDE_ARM_RATIO = 0.18f

        /** Faint enough to read as guidance rather than compete with the tracking square. */
        private const val AIM_GUIDE_COLOR = 0x80FFFFFF.toInt()

        /**
         * Time to cover ~63% of the remaining distance. Raise the first to calm a shimmering
         * square at rest; lower the second if it still trails a moving face.
         */
        private const val GLIDE_TIME_CONSTANT_STILL_MS = 250f
        private const val GLIDE_TIME_CONSTANT_MOVING_MS = 15f

        /** Roughly four 60Hz frames, so a long gap between draws still resolves quickly. */
        private const val MAX_FRAME_DELTA_MS = 64L
    }
}
