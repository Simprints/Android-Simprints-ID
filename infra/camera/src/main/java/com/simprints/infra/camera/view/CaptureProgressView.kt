package com.simprints.infra.camera.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.os.BundleCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extensions.dpToPx
import com.simprints.infra.camera.R

@ExcludedFromGeneratedTestCoverageReports("UI Code")
class CaptureProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var progressAnimator: ValueAnimator? = null
    val isAnimating: Boolean get() = progressAnimator?.isRunning == true
    private var max: Int = 100
        set(value) {
            field = value.coerceAtLeast(1)
            invalidate()
        }

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, max)
            invalidate()
        }

    @ColorInt
    private var progressColor: Int = Color.GREEN
        set(value) {
            field = value
            progressPaint.color = value
            invalidate()
        }

    @ColorInt
    private var chipStrokeColor: Int? = null
        set(value) {
            field = value
            chipStrokePaint.color = value ?: Color.TRANSPARENT
            invalidate()
        }

    private var chipStrokeWidth: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
            updateChipStrokePaintWidth()
            invalidate()
        }

    private var targetViewId: Int = NO_ID
    private var resolvedTarget: CaptureTargetView? = null
    private var chipHeight: Float = 12f.dpToPx(context)
    private var chipGap: Float = 0f
    private var chipCount: Int = 0
    private var perimeterLength = 0f
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val chipStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    // Reusable objects for drawing
    private val singleChipPath = Path()
    private val perimeterPath = Path() // the full outline chips are drawn along
    private val perimeterMeasure = PathMeasure() // length of drawing perimeter
    // Draw Path cache. Only rebuilt when the target's geometry actually changes
    private var cachedTargetOffsetX = Float.NaN
    private var cachedTargetOffsetY = Float.NaN
    private var cachedTargetWidth = -1
    private var cachedTargetHeight = -1
    private var cachedOuterStrokeEdge = Float.NaN

    init {
        setWillNotDraw(false)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.CaptureProgressView)
            try {
                targetViewId = ta.getResourceId(R.styleable.CaptureProgressView_targetViewId, NO_ID)
                progressColor = ta.getColor(R.styleable.CaptureProgressView_progressColor, progressColor)
                chipHeight = ta.getDimension(R.styleable.CaptureProgressView_chipHeight, chipHeight)
                chipGap = ta.getDimension(R.styleable.CaptureProgressView_chipGap, chipGap)
                chipCount = ta.getInt(R.styleable.CaptureProgressView_chipCount, chipCount)
                chipStrokeColor = if (ta.hasValue(R.styleable.CaptureProgressView_chipStrokeColor)) {
                    ta.getColor(R.styleable.CaptureProgressView_chipStrokeColor, Color.TRANSPARENT)
                } else {
                    null
                }
                chipStrokeWidth = ta.getDimension(R.styleable.CaptureProgressView_chipStrokeWidth, 0f)
                progress = ta.getInt(R.styleable.CaptureProgressView_progress, progress)
            } finally {
                ta.recycle()
            }
        }

        progressPaint.strokeWidth = chipHeight
        updateChipStrokePaintWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val target = resolveTarget()

        if (isInEditMode) {
            // In edit mode: keep retrying until the target is found and has been laid out.
            // layout() sets left/top/width/height; a zero-size target means layout isn't done yet.
            if (target == null || target.width == 0) {
                invalidate()
                return
            }
        } else {
            if (target == null) return
        }

        // Drawing path only if the target view attributes changed since last draw
        if (isTargetGeometryChanged(target)) {
            rebuildDrawPath(target)
        }

        // If the target view is not drawn yet
        if (perimeterLength == 0f) return

        drawCompletedChips(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Reset cache so the next draw picks up the sibling's updated left/top after layout
        cachedTargetOffsetX = Float.NaN
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(BUNDLE_ID_SAVE_INSTANCE_STATE, super.onSaveInstanceState())
        putInt(BUNDLE_ID_PROGRESS, progress)
        putInt(BUNDLE_ID_MAX, max)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            progress = state.getInt(BUNDLE_ID_PROGRESS, progress)
            max = state.getInt(BUNDLE_ID_MAX, max)
            super.onRestoreInstanceState(BundleCompat.getParcelable(state, BUNDLE_ID_SAVE_INSTANCE_STATE, BaseSavedState::class.java))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
        progressAnimator = null
    }

    fun setProgressAnimated(
        value: Int,
        durationMs: Long = 200L,
        interpolator: TimeInterpolator = LinearInterpolator(),
        onComplete: (() -> Unit)? = null,
    ) {
        progressAnimator?.cancel()
        progressAnimator = ValueAnimator.ofInt(progress, value).apply {
            duration = durationMs
            this.interpolator = interpolator
            addUpdateListener { progress = it.animatedValue as Int }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    /**
     * Draws only the chips that correspond to the current progress fraction
     * */
    private fun drawCompletedChips(canvas: Canvas) {
        // converts progress ratio into how many chips should be visible.
        // I.e: 50% progress with 10 chips = 5 chips to display.
        val completedChips = (progress.toFloat() / max.toFloat() * chipCount).toInt()
        if (completedChips == 0) return

        val chipWidth = computeChipWidth()
        val slotWidth = chipWidth + chipGap // the full space one chip occupies including the gap after it.
        val isDrawingStroke = chipStrokeColor != null && chipStrokeWidth > 0f

        for (index in 0 until completedChips) {
            val chipStart = index * slotWidth
            // Reseting path object coordinates
            singleChipPath.rewind()
            // creating chip 'path' object that later can be painted on canvas
            createChipSegmentPath(startLength = chipStart, endLength = chipStart + chipWidth, dest = singleChipPath)

            // Draw stroke border underneath the chip fill
            if (isDrawingStroke) {
                canvas.drawPath(singleChipPath, chipStrokePaint)
            }

            canvas.drawPath(singleChipPath, progressPaint)
        }
    }

    private fun cacheTargetGeometry(target: CaptureTargetView) {
        cachedTargetOffsetX = (target.left - left).toFloat()
        cachedTargetOffsetY = (target.top - top).toFloat()
        cachedTargetWidth = target.width
        cachedTargetHeight = target.height
        cachedOuterStrokeEdge = target.outerStrokeEdge
    }

    /**
     * Compares the geometry attributes of the target view with the cached values. If no changes, it indicates that the target view
     * hasn't changed since last pass.
     */
    private fun isTargetGeometryChanged(target: CaptureTargetView): Boolean = (target.left - left).toFloat() != cachedTargetOffsetX ||
        (target.top - top).toFloat() != cachedTargetOffsetY ||
        target.width != cachedTargetWidth ||
        target.height != cachedTargetHeight ||
        target.outerStrokeEdge != cachedOuterStrokeEdge

    /**
     * Creates a path for loading chips. It has an enlarged copy of the target view's shape: it is pushed outward so that chips are drawn
     * just outside the target's border rather than on top of it.
     *
     * The path starts at 12 o'clock and goes clockwise, built with direct geometry so it works
     * in both runtime and Android Studio's LayoutLib preview (where PathMeasure position lookups are broken).
     */
    private fun rebuildDrawPath(target: CaptureTargetView) {
        cacheTargetGeometry(target)

        val cornerCorrection = 1f.dpToPx(context)
        val outset = chipHeight / 2f - cornerCorrection // correction is required to properly align with corner arcs

        val localLeft = (target.left - left).toFloat()
        val localTop = (target.top - top).toFloat()
        val localRight = localLeft + target.width
        val localBottom = localTop + target.height

        // Expanding this view outward by outset on all sides: pushing the chip path just outside the border of a target so the loading
        // indicator wraps the target view
        val expandedBounds = RectF(
            localLeft - outset,
            localTop - outset,
            localRight + outset,
            localBottom + outset,
        )

        val expandedRadius = when (target.shape) {
            CaptureTargetView.Shape.OVAL -> 0f // unused for oval
            CaptureTargetView.Shape.RECT -> {
                val maxRadius = minOf(expandedBounds.width(), expandedBounds.height()) / 2f
                (target.cornerRadius + outset).coerceAtMost(maxRadius)
            }
        }

        buildPerimeterPath(target.shape, expandedBounds, expandedRadius)
        perimeterMeasure.setPath(perimeterPath, false)
        perimeterLength = perimeterMeasure.length
    }

    /**
     * Builds [perimeterPath] starting at 12 o'clock and going clockwise using direct geometry,
     * avoiding PathMeasure position lookups which are broken in Android Studio's LayoutLib.
     */
    private fun buildPerimeterPath(
        shape: CaptureTargetView.Shape,
        expandedBounds: RectF,
        expandedRadius: Float,
    ) {
        perimeterPath.rewind()
        val topCentreX = expandedBounds.centerX()
        val topCentreY = expandedBounds.top
        when (shape) {
            CaptureTargetView.Shape.OVAL -> {
                perimeterPath.moveTo(topCentreX, topCentreY)
                perimeterPath.arcTo(expandedBounds, -90f, 90f)  // 12 → 3
                perimeterPath.arcTo(expandedBounds, 0f, 90f)    // 3 → 6
                perimeterPath.arcTo(expandedBounds, 90f, 90f)   // 6 → 9
                perimeterPath.arcTo(expandedBounds, 180f, 90f)  // 9 → 12
            }
            CaptureTargetView.Shape.RECT -> {
                perimeterPath.moveTo(topCentreX, topCentreY)
                if (expandedRadius > 0f) {
                    val r2 = 2f * expandedRadius
                    // Top edge: top-center → top-right corner arc start
                    perimeterPath.lineTo(expandedBounds.right - expandedRadius, topCentreY)
                    // Top-right corner
                    perimeterPath.arcTo(RectF(expandedBounds.right - r2, expandedBounds.top, expandedBounds.right, expandedBounds.top + r2), -90f, 90f)
                    // Right edge
                    perimeterPath.lineTo(expandedBounds.right, expandedBounds.bottom - expandedRadius)
                    // Bottom-right corner
                    perimeterPath.arcTo(RectF(expandedBounds.right - r2, expandedBounds.bottom - r2, expandedBounds.right, expandedBounds.bottom), 0f, 90f)
                    // Bottom edge
                    perimeterPath.lineTo(expandedBounds.left + expandedRadius, expandedBounds.bottom)
                    // Bottom-left corner
                    perimeterPath.arcTo(RectF(expandedBounds.left, expandedBounds.bottom - r2, expandedBounds.left + r2, expandedBounds.bottom), 90f, 90f)
                    // Left edge
                    perimeterPath.lineTo(expandedBounds.left, expandedBounds.top + expandedRadius)
                    // Top-left corner
                    perimeterPath.arcTo(RectF(expandedBounds.left, expandedBounds.top, expandedBounds.left + r2, expandedBounds.top + r2), 180f, 90f)
                } else {
                    perimeterPath.lineTo(expandedBounds.right, expandedBounds.top)
                    perimeterPath.lineTo(expandedBounds.right, expandedBounds.bottom)
                    perimeterPath.lineTo(expandedBounds.left, expandedBounds.bottom)
                    perimeterPath.lineTo(expandedBounds.left, expandedBounds.top)
                }
                // Close back to top-center
                perimeterPath.lineTo(topCentreX, topCentreY)
            }
        }
        perimeterPath.close()
    }

    /**
     *Width of a single chip is derived from perimeter length, chip count, and gap.
     */
    private fun computeChipWidth(): Float {
        val totalGapLength = chipCount * chipGap
        return ((perimeterLength - totalGapLength) / chipCount).coerceAtLeast(1f)
    }

    /**
     * Takes out one chip's size of path from the full perimeter and puts it in [dest]. It becomes ready to paint afterwards.
     */
    private fun createChipSegmentPath(
        startLength: Float,
        endLength: Float,
        dest: Path,
    ) {
        if (endLength <= startLength) return
        perimeterMeasure.getSegment(
            startLength.coerceAtLeast(0f),
            endLength.coerceAtMost(perimeterLength),
            dest,
            true,
        )
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

    /**
     * Makes troke paint wider, so that it extends past edges of the chip fill. Otherwise, it won't be visible because
     * the stroke would be the same width as the chip fill and drawn underneath it.
     */
    private fun updateChipStrokePaintWidth() {
        chipStrokePaint.strokeWidth = chipHeight + chipStrokeWidth * 2f
    }

    companion object {
        private const val BUNDLE_ID_SAVE_INSTANCE_STATE = "BUNDLE_ID_SAVE_INSTANCE_STATE"
        private const val BUNDLE_ID_PROGRESS = "BUNDLE_ID_PROGRESS"
        private const val BUNDLE_ID_MAX = "BUNDLE_ID_MAX"
    }
}
