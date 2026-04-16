package com.simprints.infra.view.imagecapture

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
import com.simprints.core.tools.extentions.dpToPx
import com.simprints.infra.uibase.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

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
    private val expandedPath = Path() // path of the target's shape pushed outwards
    private val perimeterMeasure = PathMeasure() // length of drawing perimeter
    private val tempMeasures = PathMeasure() // intermediate paths measures. Used during perimeter construction
    private val selfWindowLocation = IntArray(2) // This view's top-left corner in screen coordinates
    private val startPositionXY = FloatArray(2) // Buffer for the 12 o'clock pixel coordinate

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

        // In edit mode, retry every frame until the hierarchy is ready
        // Without this check the XML preview is laggy and impossible to work with
        if (target == null) {
            if (!isInEditMode) return
            invalidate()
            return
        }

        // Drawing path only if the target view attributes changed since last draw
        if (isTargetGeometryChanged(target)) {
            rebuildDrawPath(target)
        }

        // If the target view is not drawn yet
        if (perimeterLength == 0f) return

        drawCompletedChips(canvas)
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
        cachedTargetOffsetX = target.windowOffsetX
        cachedTargetOffsetY = target.windowOffsetY
        cachedTargetWidth = target.width
        cachedTargetHeight = target.height
        cachedOuterStrokeEdge = target.outerStrokeEdge
    }

    /**
     * Compares the geometry attributes of the target view with the cached values. If no changes, it indicates that the target view
     * hasn't changed since last pass.
     */
    private fun isTargetGeometryChanged(target: CaptureTargetView): Boolean = target.windowOffsetX != cachedTargetOffsetX ||
        target.windowOffsetY != cachedTargetOffsetY ||
        target.width != cachedTargetWidth ||
        target.height != cachedTargetHeight ||
        target.outerStrokeEdge != cachedOuterStrokeEdge

    /**
     * Creates a path for loading chips. It has an enlarged copy of the target view's shape: it is pushed outward so that chips are drawn
     * just outside the target's border rather than on top of it.
     *
     * The shape is then adjusted to start at 12 o'clock, and then progress draws chips clockwise from the top middle.
     */
    private fun rebuildDrawPath(target: CaptureTargetView) {
        cacheTargetGeometry(target)

        val cornerCorrection = 1f.dpToPx(context)
        val outset = chipHeight / 2f - cornerCorrection // correction is required to properly align with corner arcs

        // Saving view's position on the screen
        getLocationInWindow(selfWindowLocation)

        // converting target's screen coordinates into coordinates relative to this view
        val localLeft = target.windowOffsetX - selfWindowLocation[0]
        val localTop = target.windowOffsetY - selfWindowLocation[1]
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

        expandedPath.rewind()
        val expandedRadius = when (target.shape) {
            CaptureTargetView.Shape.OVAL -> 0f // unused for oval
            CaptureTargetView.Shape.RECT -> {
                val maxRadius = minOf(expandedBounds.width(), expandedBounds.height()) / 2f
                (target.cornerRadius + outset).coerceAtMost(maxRadius)
            }
        }
        when (target.shape) {
            CaptureTargetView.Shape.OVAL -> expandedPath.addOval(expandedBounds, Path.Direction.CW)
            CaptureTargetView.Shape.RECT -> expandedPath.addRoundRect(expandedBounds, expandedRadius, expandedRadius, Path.Direction.CW)
        }

        buildSingleContourStartingAtTopCentre(expandedPath, target.shape, expandedBounds, expandedRadius)
        perimeterMeasure.setPath(perimeterPath, false)
        perimeterLength = perimeterMeasure.length
    }

    /**
     * Rewrites [sourcePath] into [perimeterPath] with the start point shifted to the top centre,
     * so that chips draw clockwise from 12 o'clock.
     */
    private fun buildSingleContourStartingAtTopCentre(
        sourcePath: Path,
        shape: CaptureTargetView.Shape,
        expandedBounds: RectF,
        expandedRadius: Float,
    ) {
        perimeterPath.rewind()
        tempMeasures.setPath(sourcePath, false)
        val totalLength = tempMeasures.length
        if (totalLength == 0f) {
            perimeterLength = 0f
            return
        }

        val topCentreOffset = when (shape) {
            // Android draws ovals from 3 o'clock clockwise, so 12 o'clock is always 25% along
            CaptureTargetView.Shape.OVAL -> totalLength * 0.75f
            CaptureTargetView.Shape.RECT -> {
                // Drawing start at the top left end of the corner arc.
                // We need to move 1 vertical line up, 1 top left corner arc, and 1/2 of horizontal line
                val arcLength = (Math.PI / 2.0 * expandedRadius).toFloat()
                val straightWidth = expandedBounds.width() - 2f * expandedRadius
                val straightHeight = expandedBounds.height() - 2f * expandedRadius
                straightHeight + arcLength + straightWidth / 2f
            }
        }

        // Getting XY coordinates at the 12 o'clock position. Saving into positionScratch.
        tempMeasures.getPosTan(topCentreOffset, startPositionXY, null)
        // Moving to 12 o'clock coordinate for the first chip
        perimeterPath.moveTo(startPositionXY[0], startPositionXY[1])
        // Going from 12 o'clock all the way to the path's end point
        tempMeasures.getSegment(topCentreOffset, totalLength, perimeterPath, false)

        // Continuing drawing from the path's end point all the way to 12 o'clock
        tempMeasures.setPath(sourcePath, false)
        tempMeasures.getSegment(0f, topCentreOffset, perimeterPath, false)

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
