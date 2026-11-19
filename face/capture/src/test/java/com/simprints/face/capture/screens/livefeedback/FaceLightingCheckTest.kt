package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.camera.lighting.ClassifyLightingConditionsUseCase
import com.simprints.infra.camera.lighting.LightingStats
import com.simprints.infra.camera.lighting.MeasureLightingStatsUseCase
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration
import com.simprints.infra.config.store.models.FaceLightingConditionsAssessmentMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

class FaceLightingCheckTest {
    private val bitmap = mockk<Bitmap>()
    private val measureLightingStats = mockk<MeasureLightingStatsUseCase>()
    private lateinit var check: FaceLightingCheck

    @Before
    fun setUp() {
        every { measureLightingStats(any(), any(), any()) } returns NORMAL_STATS
        check = FaceLightingCheck(measureLightingStats, ClassifyLightingConditionsUseCase())
    }

    @Test
    fun `off by default`() {
        check.configure(config())

        assertThat(check.mode).isEqualTo(FaceLightingConditionsAssessmentMode.OFF)
        assertThat(check.measureIfDue(bitmap, null, 0)).isNull()
        assertThat(check.updateHint(isNoFace = true, nowMs = 0)).isFalse()
        verify(exactly = 0) { measureLightingStats(any(), any(), any()) }
    }

    @Test
    fun `measures the face box without a border, or the circle with a border`() {
        check.configure(config(mode = "LOG", intervalMs = 0))
        val faceBox = mockk<RectF>()

        check.measureIfDue(bitmap, faceBox, 0)
        check.measureIfDue(bitmap, null, 1)

        verify { measureLightingStats(bitmap, faceBox, 0) }
        verify { measureLightingStats(bitmap, null, 5) }
    }

    @Test
    fun `measures at most once per interval`() {
        check.configure(config(mode = "LOG", intervalMs = 250))

        assertThat(check.measureIfDue(bitmap, null, 1_000)).isNotNull()
        assertThat(check.measureIfDue(bitmap, null, 1_249)).isNull()
        assertThat(check.measureIfDue(bitmap, null, 1_250)).isNotNull()
        verify(exactly = 2) { measureLightingStats(any(), any(), any()) }
    }

    @Test
    fun `too dark below the default 14 percent median brightness`() {
        check.configure(config(mode = "LOG", intervalMs = 0))

        every { measureLightingStats(any(), any(), any()) } returns LightingStats(p5 = 0, median = 35, p95 = 200)
        assertThat(check.measureIfDue(bitmap, null, 0)?.isTooDark).isTrue()

        every { measureLightingStats(any(), any(), any()) } returns LightingStats(p5 = 0, median = 36, p95 = 200)
        assertThat(check.measureIfDue(bitmap, null, 1)?.isTooDark).isFalse()
    }

    @Test
    fun `never reports bright frames`() {
        check.configure(config(mode = "LOG", intervalMs = 0))
        every { measureLightingStats(any(), any(), any()) } returns LightingStats(p5 = 254, median = 255, p95 = 255)

        assertThat(check.measureIfDue(bitmap, null, 0)?.isTooDark).isFalse()
    }

    @Test
    fun `log mode never shows the hint`() {
        check.configure(config(mode = "LOG", intervalMs = 0, debounceMs = 0))
        every { measureLightingStats(any(), any(), any()) } returns DARK_STATS
        check.measureIfDue(bitmap, null, 0)

        assertThat(check.updateHint(isNoFace = true, nowMs = 0)).isFalse()
    }

    @Test
    fun `hint mode shows the hint only for no face on a too-dark frame, after the debounce`() {
        check.configure(config(mode = "HINT", intervalMs = 0, debounceMs = 500))
        every { measureLightingStats(any(), any(), any()) } returns DARK_STATS
        check.measureIfDue(bitmap, null, 0)

        assertThat(check.updateHint(isNoFace = false, nowMs = 0)).isFalse()
        assertThat(check.updateHint(isNoFace = true, nowMs = 100)).isFalse()
        assertThat(check.updateHint(isNoFace = true, nowMs = 600)).isTrue()
    }

    @Test
    fun `hint mode shows no hint for a no-face frame in normal light`() {
        check.configure(config(mode = "HINT", intervalMs = 0, debounceMs = 0))
        check.measureIfDue(bitmap, null, 0)

        assertThat(check.updateHint(isNoFace = true, nowMs = 0)).isFalse()
    }

    @Test
    fun `reset forgets the last measurement and hides the hint`() {
        check.configure(config(mode = "HINT", intervalMs = 1_000, debounceMs = 0))
        every { measureLightingStats(any(), any(), any()) } returns DARK_STATS
        check.measureIfDue(bitmap, null, 0)
        assertThat(check.updateHint(isNoFace = true, nowMs = 0)).isTrue()

        check.reset()

        assertThat(check.isTooDark).isFalse()
        assertThat(check.updateHint(isNoFace = true, nowMs = 1)).isFalse()
        assertThat(check.measureIfDue(bitmap, null, 2)).isNotNull()
    }

    private fun config(
        mode: String? = null,
        intervalMs: Long? = null,
        debounceMs: Long? = null,
    ) = ExperimentalProjectConfiguration(
        buildMap {
            mode?.let { put(ExperimentalProjectConfiguration.FACE_LIGHTING_CONDITIONS_ASSESSMENT_MODE, JsonPrimitive(it)) }
            intervalMs?.let { put(ExperimentalProjectConfiguration.FACE_LIGHTING_CONDITIONS_ASSESSMENT_INTERVAL_MILLIS, JsonPrimitive(it)) }
            debounceMs?.let {
                put(ExperimentalProjectConfiguration.FACE_LIGHTING_CONDITIONS_ASSESSMENT_HINT_DEBOUNCE_MILLIS, JsonPrimitive(it))
            }
        },
    )

    private companion object {
        val NORMAL_STATS = LightingStats(p5 = 40, median = 120, p95 = 200)
        val DARK_STATS = LightingStats(p5 = 0, median = 10, p95 = 30)
    }
}
