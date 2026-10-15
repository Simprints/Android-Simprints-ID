package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RunStatus
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class AutoCaptureAttemptStatsTest {
    private val bitmap: Bitmap = mockk(relaxed = true)
    private lateinit var stats: AutoCaptureAttemptStats

    @Before
    fun setUp() {
        stats = AutoCaptureAttemptStats(
            attemptNb = 2,
            bioSdk = "RANK_ONE",
            startTime = Timestamp(START_MS),
            random = Random(SEED),
        )
    }

    @Test
    fun `empty attempt builds an event with zero counts and no runs`() {
        val payload = stats.toEvent(Timestamp(START_MS + 500)).payload

        assertThat(payload.attemptNb).isEqualTo(2)
        assertThat(payload.bioSdk).isEqualTo("RANK_ONE")
        assertThat(payload.createdAt).isEqualTo(Timestamp(START_MS))
        assertThat(payload.endedAt).isEqualTo(Timestamp(START_MS + 500))
        assertThat(payload.totalFramesAnalysed).isEqualTo(0)
        assertThat(payload.validFrameCount).isEqualTo(0)
        assertThat(payload.timeToFirstValidMs).isNull()
        assertThat(payload.targetFrameWidth).isNull()
        assertThat(payload.qualityAllFrames).isNull()
        assertThat(payload.statusRuns).isEmpty()
        assertThat(payload.statusRunsTruncated).isFalse()
        assertThat(payload.rejectionStats.tooFar).isNull()
    }

    @Test
    fun `counts frames per reason and records frame dimensions from the first frame`() {
        record(FaceDetection.Status.TOOFAR, area = 0.12f, atMs = 0)
        record(FaceDetection.Status.TOOFAR, area = 0.18f, atMs = 100)
        record(FaceDetection.Status.OFFYAW, yaw = -41f, atMs = 200)
        record(FaceDetection.Status.VALID, atMs = 300)
        record(FaceDetection.Status.VALID_CAPTURING, atMs = 400)

        val payload = stats.toEvent(Timestamp(START_MS + 500)).payload

        assertThat(payload.totalFramesAnalysed).isEqualTo(5)
        assertThat(payload.validFrameCount).isEqualTo(2)
        assertThat(payload.timeToFirstValidMs).isEqualTo(300L)
        assertThat(payload.targetFrameWidth).isEqualTo(FRAME_W)
        assertThat(payload.targetFrameHeight).isEqualTo(FRAME_H)
        assertThat(payload.rejectionStats.tooFar?.count).isEqualTo(2)
        assertThat(payload.rejectionStats.offYaw?.count).isEqualTo(1)
        assertThat(payload.rejectionStats.badQuality).isNull()
    }

    @Test
    fun `records the value of the check that failed, with min max and median`() {
        record(FaceDetection.Status.TOOFAR, area = 0.18f, atMs = 0)
        record(FaceDetection.Status.TOOFAR, area = 0.12f, atMs = 50)
        record(FaceDetection.Status.TOOFAR, area = 0.15f, atMs = 100)
        record(FaceDetection.Status.OFFROLL, roll = 22f, atMs = 150)
        record(FaceDetection.Status.OFFROLL, roll = -30f, atMs = 200)
        record(FaceDetection.Status.BAD_QUALITY, quality = 0.3f, atMs = 250)

        val rs = stats.toEvent(Timestamp(START_MS + 300)).payload.rejectionStats

        with(rs.tooFar!!) {
            assertThat(minValue).isEqualTo(0.12f)
            assertThat(maxValue).isEqualTo(0.18f)
            assertThat(medianValue).isEqualTo(0.15f)
        }
        with(rs.offRoll!!) {
            assertThat(minValue).isEqualTo(-30f)
            assertThat(maxValue).isEqualTo(22f)
            assertThat(medianValue).isEqualTo(-4f)
        }
        assertThat(rs.badQuality?.medianValue).isEqualTo(0.3f)
    }

    @Test
    fun `invalid frames carry no value statistics`() {
        record(FaceDetection.Status.NOFACE, atMs = 0)
        record(FaceDetection.Status.NOFACE, atMs = 100)

        val invalid = stats
            .toEvent(Timestamp(START_MS + 200))
            .payload.rejectionStats.invalid!!

        assertThat(invalid.count).isEqualTo(2)
        assertThat(invalid.minValue).isNull()
        assertThat(invalid.maxValue).isNull()
        assertThat(invalid.medianValue).isNull()
    }

    @Test
    fun `quality across all frames includes frames rejected for geometry`() {
        record(FaceDetection.Status.TOOFAR, area = 0.1f, quality = 0.9f, atMs = 0)
        record(FaceDetection.Status.OFFYAW, yaw = 40f, quality = 0.5f, atMs = 100)
        record(FaceDetection.Status.NOFACE, atMs = 200)
        record(FaceDetection.Status.VALID, quality = 0.7f, atMs = 300)

        val q = stats.toEvent(Timestamp(START_MS + 400)).payload.qualityAllFrames!!

        assertThat(q.minValue).isEqualTo(0.5f)
        assertThat(q.maxValue).isEqualTo(0.9f)
        assertThat(q.medianValue).isEqualTo(0.7f)
    }

    @Test
    fun `firstSeenMs and longestRunMs are measured from attempt start`() {
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 0)
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 100)
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 200)
        record(FaceDetection.Status.OFFYAW, yaw = 40f, atMs = 300)
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 450)
        record(FaceDetection.Status.VALID, atMs = 500)

        val payload = stats.toEvent(Timestamp(START_MS + 900)).payload

        with(payload.rejectionStats.tooFar!!) {
            assertThat(firstSeenMs).isEqualTo(0L)
            assertThat(longestRunMs).isEqualTo(300L)
            assertThat(count).isEqualTo(4)
        }
        with(payload.rejectionStats.offYaw!!) {
            assertThat(firstSeenMs).isEqualTo(300L)
            assertThat(longestRunMs).isEqualTo(150L)
        }
        assertThat(payload.timeToFirstValidMs).isEqualTo(500L)
    }

    @Test
    fun `status runs are run-length encoded in order with frame counts and durations`() {
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 0)
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 100)
        record(FaceDetection.Status.OFFYAW, yaw = 40f, atMs = 300)
        record(FaceDetection.Status.VALID, atMs = 500)
        record(FaceDetection.Status.VALID_CAPTURING, atMs = 600)
        record(FaceDetection.Status.VALID_CAPTURING, atMs = 700)

        val runs = stats.toEvent(Timestamp(START_MS + 1000)).payload.statusRuns

        assertThat(runs.map { it.status }).containsExactly(RunStatus.TOO_FAR, RunStatus.OFF_YAW, RunStatus.VALID).inOrder()
        assertThat(runs.map { it.frameCount }).containsExactly(2, 1, 3).inOrder()
        assertThat(runs.map { it.durationMs }).containsExactly(300L, 200L, 500L).inOrder()
    }

    @Test
    fun `status runs are capped and the truncation flag is set, while counts keep accumulating`() {
        val max = FaceCaptureAttemptEvent.MAX_STATUS_RUNS
        repeat(max + 50) { i ->
            if (i % 2 == 0) {
                record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = i * 10L)
            } else {
                record(FaceDetection.Status.NOFACE, atMs = i * 10L)
            }
        }

        val payload = stats.toEvent(Timestamp(START_MS + (max + 50) * 10L)).payload

        assertThat(payload.statusRuns).hasSize(max)
        assertThat(payload.statusRunsTruncated).isTrue()
        assertThat(payload.totalFramesAnalysed).isEqualTo(max + 50)
        assertThat((payload.rejectionStats.tooFar?.count ?: 0) + (payload.rejectionStats.invalid?.count ?: 0))
            .isEqualTo(max + 50)
    }

    @Test
    fun `snapshot and restore preserve the attempt across process death`() {
        record(FaceDetection.Status.TOOFAR, area = 0.12f, atMs = 0)
        record(FaceDetection.Status.OFFYAW, yaw = 40f, quality = 0.8f, atMs = 200)
        record(FaceDetection.Status.VALID, quality = 0.9f, atMs = 400)

        val restored = AutoCaptureAttemptStats.restore(stats.snapshot())!!

        assertThat(restored.attemptNb).isEqualTo(2)
        assertThat(restored.startTime).isEqualTo(Timestamp(START_MS))
        assertThat(restored.lastFrameTime).isEqualTo(Timestamp(START_MS + 400))

        val fromRestored = restored.toEvent(Timestamp(START_MS + 600)).payload
        val fromOriginal = stats.toEvent(Timestamp(START_MS + 600)).payload

        assertThat(fromRestored.totalFramesAnalysed).isEqualTo(fromOriginal.totalFramesAnalysed)
        assertThat(fromRestored.rejectionStats).isEqualTo(fromOriginal.rejectionStats)
        assertThat(fromRestored.statusRuns).isEqualTo(fromOriginal.statusRuns)
        assertThat(fromRestored.qualityAllFrames).isEqualTo(fromOriginal.qualityAllFrames)
        assertThat(fromRestored.timeToFirstValidMs).isEqualTo(400L)
    }

    @Test
    fun `restored attempt keeps the original event id`() {
        record(FaceDetection.Status.TOOFAR, area = 0.12f, atMs = 0)
        val restored = AutoCaptureAttemptStats.restore(stats.snapshot())!!

        assertThat(restored.toEvent(Timestamp(START_MS + 100)).id).isEqualTo(stats.toEvent(Timestamp(START_MS + 100)).id)
    }

    @Test
    fun `restore returns null for a corrupt snapshot`() {
        assertThat(AutoCaptureAttemptStats.restore("not json")).isNull()
    }

    @Test
    fun `value samples are bounded while min and max stay exact and the median is estimated`() {
        val n = MANY_FRAMES
        val areas = (0 until n).map { it / n.toFloat() }.shuffled(Random(SEED))
        areas.forEachIndexed { i, area -> record(FaceDetection.Status.TOOFAR, area = area, atMs = i * 33L) }

        val tooFar = stats
            .toEvent(Timestamp(START_MS + n * 33L))
            .payload.rejectionStats.tooFar!!

        assertThat(tooFar.count).isEqualTo(n)
        assertThat(tooFar.minValue).isEqualTo(0f)
        assertThat(tooFar.maxValue).isWithin(0.0001f).of((n - 1) / n.toFloat())
        assertThat(tooFar.medianValue).isWithin(0.1f).of(0.5f)
    }

    @Test
    fun `snapshot size stays bounded for a long attempt with a status flickering every frame`() {
        val n = MANY_FRAMES
        repeat(n) { i ->
            if (i % 2 == 0) {
                record(FaceDetection.Status.TOOFAR, area = i / n.toFloat(), quality = i / n.toFloat(), atMs = i * 33L)
            } else {
                record(FaceDetection.Status.OFFYAW, yaw = i.toFloat(), quality = i / n.toFloat(), atMs = i * 33L)
            }
        }

        val snapshot = stats.snapshot()

        assertThat(snapshot.length).isLessThan(60_000)
        val restored = AutoCaptureAttemptStats.restore(snapshot)!!
        val payload = restored.toEvent(Timestamp(START_MS + n * 33L)).payload
        assertThat(payload.totalFramesAnalysed).isEqualTo(n)
        assertThat(payload.statusRuns).hasSize(FaceCaptureAttemptEvent.MAX_STATUS_RUNS)
        assertThat(payload.statusRunsTruncated).isTrue()
    }

    @Test
    fun `snapshotIfDue persists the first frame, then throttles by frame time regardless of status changes`() {
        assertThat(stats.snapshotIfDue()).isNull()

        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 0)
        assertThat(stats.snapshotIfDue()).isNotNull()
        assertThat(stats.snapshotIfDue()).isNull()

        record(FaceDetection.Status.NOFACE, atMs = 100)
        record(FaceDetection.Status.TOOFAR, area = 0.1f, atMs = 200)
        record(FaceDetection.Status.NOFACE, atMs = 300)
        assertThat(stats.snapshotIfDue()).isNull()

        record(FaceDetection.Status.NOFACE, atMs = AutoCaptureAttemptStats.SNAPSHOT_INTERVAL_MS)
        assertThat(stats.snapshotIfDue()).isNotNull()
        assertThat(stats.snapshotIfDue()).isNull()
    }

    private fun record(
        status: FaceDetection.Status,
        area: Float? = null,
        yaw: Float = 0f,
        roll: Float = 0f,
        quality: Float = 1f,
        atMs: Long,
    ) {
        val face = if (status == FaceDetection.Status.NOFACE) {
            null
        } else {
            Face(100, 100, Rect(0, 0, 60, 60), yaw, roll, quality, ByteArray(0), "format")
        }
        val detection = FaceDetection(
            original = bitmap,
            bitmap = bitmap,
            face = face,
            status = status,
            detectionStartTime = Timestamp(START_MS + atMs),
            detectionEndTime = Timestamp(START_MS + atMs),
        )
        stats.recordFrame(
            detection = detection,
            areaOccupied = area,
            frameTime = Timestamp(START_MS + atMs),
            frameWidth = FRAME_W,
            frameHeight = FRAME_H,
        )
    }

    companion object {
        private const val START_MS = 1_000_000L
        private const val FRAME_W = 556
        private const val FRAME_H = 556
        private const val SEED = 42

        private const val MANY_FRAMES = 2_000
    }
}
