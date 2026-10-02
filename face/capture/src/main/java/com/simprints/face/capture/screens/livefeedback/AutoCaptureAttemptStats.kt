package com.simprints.face.capture.screens.livefeedback

import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.BioSdk
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RejectionMetric
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RejectionStats
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RunStatus
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.StatusRun
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.ValueStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Accumulates per-attempt auto-capture diagnostics (MS-1605) from the frames the analyser
 * evaluates, and turns them into a [FaceCaptureAttemptEvent] when the attempt ends.
 *
 * Designed to be cheap on the analyser thread: per frame it does a handful of comparisons on
 * primitives and appends one float to a small list. Medians are computed once, at [build].
 *
 * The whole state can be captured with [snapshot] and restored with [restore], so an attempt
 * survives process death via SavedStateHandle. Not thread-safe: callers must confine it to the
 * analyser executor, as [LiveFeedbackViewModel] does.
 */
internal class AutoCaptureAttemptStats private constructor(
    private val state: State,
) {
    constructor(
        attemptNb: Int,
        bioSdk: BioSdk,
        startTime: Timestamp,
    ) : this(State(attemptNb = attemptNb, bioSdk = bioSdk, startTime = startTime))

    val attemptNb: Int get() = state.attemptNb
    val startTime: Timestamp get() = state.startTime

    /** Time of the last frame recorded; falls back to [startTime] for an attempt that saw no frames. */
    val lastFrameTime: Timestamp get() = state.lastFrameTime ?: state.startTime

    /** True once the run timeline changed since the last [snapshot] - the cue to persist. */
    var hasUnsavedRunChange: Boolean = false
        private set

    /**
     * Records one analysed frame.
     *
     * @param detection the frame's detection result; its status decides which bucket the frame lands in.
     * @param areaOccupied the face-area ratio used for the TOOFAR / TOOCLOSE decision, null when no face.
     * @param frameTime when the frame was analysed.
     * @param frameWidth width of the cropped target frame passed to the detector.
     * @param frameHeight height of the cropped target frame passed to the detector.
     */
    @Synchronized
    fun recordFrame(
        detection: FaceDetection,
        areaOccupied: Float?,
        frameTime: Timestamp,
        frameWidth: Int,
        frameHeight: Int,
    ) {
        val offsetMs = (frameTime.ms - state.startTime.ms).coerceAtLeast(0L)
        val runStatus = detection.status.toRunStatus()

        state.totalFramesAnalysed++
        state.lastFrameTime = frameTime
        if (state.targetFrameWidth == null) {
            state.targetFrameWidth = frameWidth
            state.targetFrameHeight = frameHeight
        }

        detection.face?.quality?.let { state.qualityAllFrames.add(it) }

        if (runStatus == RunStatus.VALID) {
            state.validFrameCount++
            if (state.timeToFirstValidMs == null) state.timeToFirstValidMs = offsetMs
        } else {
            val reason = state.reasons.getOrPut(runStatus) { ReasonState(firstSeenMs = offsetMs) }
            reason.count++
            decidingValue(detection, areaOccupied)?.let { reason.values.add(it) }
        }

        advanceRuns(runStatus, offsetMs)
    }

    /** Builds the event. [endTime] is when the attempt finished, or was abandoned. */
    @Synchronized
    fun build(endTime: Timestamp): FaceCaptureAttemptEvent {
        val endOffsetMs = (endTime.ms - state.startTime.ms).coerceAtLeast(0L)
        closeOpenRun(endOffsetMs)

        return FaceCaptureAttemptEvent(
            startTime = state.startTime,
            endTime = endTime,
            attemptNb = state.attemptNb,
            bioSdk = state.bioSdk,
            targetFrameWidth = state.targetFrameWidth,
            targetFrameHeight = state.targetFrameHeight,
            totalFramesAnalysed = state.totalFramesAnalysed,
            validFrameCount = state.validFrameCount,
            timeToFirstValidMs = state.timeToFirstValidMs,
            rejectionStats = RejectionStats(
                invalid = state.reasons[RunStatus.INVALID]?.toMetric(),
                badQuality = state.reasons[RunStatus.BAD_QUALITY]?.toMetric(),
                offYaw = state.reasons[RunStatus.OFF_YAW]?.toMetric(),
                offRoll = state.reasons[RunStatus.OFF_ROLL]?.toMetric(),
                tooClose = state.reasons[RunStatus.TOO_CLOSE]?.toMetric(),
                tooFar = state.reasons[RunStatus.TOO_FAR]?.toMetric(),
            ),
            qualityAllFrames = state.qualityAllFrames.toValueStats(),
            statusRuns = state.closedRuns.map { StatusRun(it.status, it.frameCount, it.durationMs) },
            statusRunsTruncated = state.statusRunsTruncated,
        )
    }

    @Synchronized
    fun snapshot(): String {
        hasUnsavedRunChange = false
        return json.encodeToString(State.serializer(), state)
    }

    // region run tracking

    private fun advanceRuns(
        status: RunStatus,
        offsetMs: Long,
    ) {
        val open = state.openRun
        if (open != null && open.status == status) {
            open.frameCount++
            return
        }
        if (open != null) {
            closeRun(open, endOffsetMs = offsetMs)
        } else {
            hasUnsavedRunChange = true // first frame of the attempt - persist so a single-run attempt survives too
        }
        state.openRun = OpenRun(status = status, startMs = offsetMs, frameCount = 1)
    }

    private fun closeOpenRun(endOffsetMs: Long) {
        state.openRun?.let { closeRun(it, endOffsetMs) }
        state.openRun = null
    }

    private fun closeRun(
        run: OpenRun,
        endOffsetMs: Long,
    ) {
        val durationMs = (endOffsetMs - run.startMs).coerceAtLeast(0L)

        state.reasons[run.status]?.let { reason ->
            if (durationMs > reason.longestRunMs) reason.longestRunMs = durationMs
        }

        if (state.closedRuns.size < FaceCaptureAttemptEvent.MAX_STATUS_RUNS) {
            state.closedRuns.add(ClosedRun(run.status, run.frameCount, durationMs))
        } else {
            state.statusRunsTruncated = true
        }
        hasUnsavedRunChange = true
    }

    // endregion

    private fun decidingValue(
        detection: FaceDetection,
        areaOccupied: Float?,
    ): Float? = when (detection.status) {
        FaceDetection.Status.TOOFAR,
        FaceDetection.Status.TOOCLOSE,
        -> areaOccupied

        FaceDetection.Status.OFFYAW -> detection.face?.yaw
        FaceDetection.Status.OFFROLL -> detection.face?.roll
        FaceDetection.Status.BAD_QUALITY -> detection.face?.quality
        FaceDetection.Status.NOFACE,
        FaceDetection.Status.VALID,
        FaceDetection.Status.VALID_CAPTURING,
        -> null
    }

    private fun ReasonState.toMetric(): RejectionMetric {
        val stats = values.toValueStats()
        return RejectionMetric(
            count = count,
            firstSeenMs = firstSeenMs,
            longestRunMs = longestRunMs,
            minValue = stats?.minValue,
            maxValue = stats?.maxValue,
            medianValue = stats?.medianValue,
        )
    }

    // region persisted state

    @Serializable
    private class State(
        val attemptNb: Int,
        val bioSdk: BioSdk,
        val startTime: Timestamp,
        var lastFrameTime: Timestamp? = null,
        var targetFrameWidth: Int? = null,
        var targetFrameHeight: Int? = null,
        var totalFramesAnalysed: Int = 0,
        var validFrameCount: Int = 0,
        var timeToFirstValidMs: Long? = null,
        val reasons: MutableMap<RunStatus, ReasonState> = mutableMapOf(),
        val qualityAllFrames: MutableList<Float> = mutableListOf(),
        val closedRuns: MutableList<ClosedRun> = mutableListOf(),
        var openRun: OpenRun? = null,
        var statusRunsTruncated: Boolean = false,
    )

    @Serializable
    private class ReasonState(
        val firstSeenMs: Long,
        var count: Int = 0,
        var longestRunMs: Long = 0L,
        val values: MutableList<Float> = mutableListOf(),
    )

    @Serializable
    private class OpenRun(
        val status: RunStatus,
        val startMs: Long,
        var frameCount: Int,
    )

    @Serializable
    private class ClosedRun(
        val status: RunStatus,
        val frameCount: Int,
        val durationMs: Long,
    )

    // endregion

    companion object {
        private val json = Json { encodeDefaults = true }

        fun restore(snapshot: String): AutoCaptureAttemptStats? = runCatching {
            AutoCaptureAttemptStats(json.decodeFromString(State.serializer(), snapshot))
        }.getOrNull()

        fun FaceDetection.Status.toRunStatus(): RunStatus = when (this) {
            FaceDetection.Status.VALID,
            FaceDetection.Status.VALID_CAPTURING,
            -> RunStatus.VALID

            FaceDetection.Status.NOFACE -> RunStatus.INVALID
            FaceDetection.Status.BAD_QUALITY -> RunStatus.BAD_QUALITY
            FaceDetection.Status.OFFYAW -> RunStatus.OFF_YAW
            FaceDetection.Status.OFFROLL -> RunStatus.OFF_ROLL
            FaceDetection.Status.TOOCLOSE -> RunStatus.TOO_CLOSE
            FaceDetection.Status.TOOFAR -> RunStatus.TOO_FAR
        }

        private fun List<Float>.toValueStats(): ValueStats? {
            if (isEmpty()) return null
            val sorted = sorted()
            val mid = sorted.size / 2
            val median = if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2f else sorted[mid]
            return ValueStats(minValue = sorted.first(), maxValue = sorted.last(), medianValue = median)
        }
    }
}
