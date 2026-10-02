package com.simprints.face.capture.screens.livefeedback

import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.face.capture.models.FaceDetection
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RejectionMetric
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RejectionStats
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RunStatus
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.StatusRun
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.ValueStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Accumulates auto-capture attempt diagnostics into a [FaceCaptureAttemptEvent].
 * Not thread-safe: confine it to the analyser executor.
 */
internal class AutoCaptureAttemptStats private constructor(
    private val state: State,
    private val random: Random,
) {
    constructor(
        attemptNb: Int,
        bioSdk: String,
        startTime: Timestamp,
        random: Random = Random.Default,
    ) : this(State(attemptNb = attemptNb, bioSdk = bioSdk, startTime = startTime), random)

    val attemptNb: Int get() = state.attemptNb
    val startTime: Timestamp get() = state.startTime

    val lastFrameTime: Timestamp get() = state.lastFrameTime ?: state.startTime

    private var dirty = false

    private var lastSnapshotMs: Long? = null

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

        detection.face?.quality?.let { state.qualityAllFrames.add(it, random) }

        if (runStatus == RunStatus.VALID) {
            state.validFrameCount++
            if (state.timeToFirstValidMs == null) state.timeToFirstValidMs = offsetMs
        } else {
            val reason = state.reasons.getOrPut(runStatus) { ReasonState(firstSeenMs = offsetMs) }
            reason.count++
            decidingValue(detection, areaOccupied)?.let { reason.values.add(it, random) }
        }

        advanceRuns(runStatus, offsetMs)
        dirty = true
    }

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
            id = state.eventId,
        )
    }

    @Synchronized
    fun snapshot(): String {
        dirty = false
        lastSnapshotMs = state.lastFrameTime?.ms
        return json.encodeToString(State.serializer(), state)
    }

    /** Snapshot on the first frame, then at most once per [SNAPSHOT_INTERVAL_MS] of frame time. */
    @Synchronized
    fun snapshotIfDue(): String? {
        if (!dirty) return null
        val nowMs = state.lastFrameTime?.ms ?: return null
        val last = lastSnapshotMs
        if (last != null && nowMs - last < SNAPSHOT_INTERVAL_MS) return null
        return snapshot()
    }

    private fun advanceRuns(
        status: RunStatus,
        offsetMs: Long,
    ) {
        val open = state.openRun
        if (open != null && open.status == status) {
            open.frameCount++
            return
        }
        if (open != null) closeRun(open, endOffsetMs = offsetMs)
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
    }

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

    @Serializable
    private class State(
        // Persisted so an attempt recovered after process death keeps its event id
        val eventId: String = randomUUID(),
        val attemptNb: Int,
        val bioSdk: String,
        val startTime: Timestamp,
        var lastFrameTime: Timestamp? = null,
        var targetFrameWidth: Int? = null,
        var targetFrameHeight: Int? = null,
        var totalFramesAnalysed: Int = 0,
        var validFrameCount: Int = 0,
        var timeToFirstValidMs: Long? = null,
        val reasons: MutableMap<RunStatus, ReasonState> = mutableMapOf(),
        val qualityAllFrames: BoundedSamples = BoundedSamples(),
        val closedRuns: MutableList<ClosedRun> = mutableListOf(),
        var openRun: OpenRun? = null,
        var statusRunsTruncated: Boolean = false,
    )

    @Serializable
    private class ReasonState(
        val firstSeenMs: Long,
        var count: Int = 0,
        var longestRunMs: Long = 0L,
        val values: BoundedSamples = BoundedSamples(),
    )

    /** Reservoir sample (Algorithm R): exact min/max, estimated median beyond [MAX_VALUE_SAMPLES]. */
    @Serializable
    private class BoundedSamples(
        var seen: Int = 0,
        var minValue: Float? = null,
        var maxValue: Float? = null,
        val samples: MutableList<Float> = mutableListOf(),
    ) {
        fun add(
            value: Float,
            random: Random,
        ) {
            seen++
            val min = minValue
            val max = maxValue
            if (min == null || value < min) minValue = value
            if (max == null || value > max) maxValue = value
            if (samples.size < MAX_VALUE_SAMPLES) {
                samples.add(value)
            } else {
                val slot = random.nextInt(seen)
                if (slot < MAX_VALUE_SAMPLES) samples[slot] = value
            }
        }

        fun toValueStats(): ValueStats? {
            val min = minValue ?: return null
            val max = maxValue ?: return null
            val sorted = samples.sorted()
            val mid = sorted.size / 2
            val median = if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2f else sorted[mid]
            return ValueStats(minValue = min, maxValue = max, medianValue = median)
        }
    }

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

    companion object {
        internal const val MAX_VALUE_SAMPLES = 128
        internal const val SNAPSHOT_INTERVAL_MS = 1_000L

        private val json = Json { encodeDefaults = true }

        fun restore(
            snapshot: String,
            random: Random = Random.Default,
        ): AutoCaptureAttemptStats? = runCatching {
            AutoCaptureAttemptStats(json.decodeFromString(State.serializer(), snapshot), random)
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
    }
}
