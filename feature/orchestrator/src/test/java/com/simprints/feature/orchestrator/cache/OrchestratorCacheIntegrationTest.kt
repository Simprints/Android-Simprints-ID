package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.slot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchestratorCacheIntegrationTest {
    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var prefs: SharedPreferences

    private var jsonHelper = JsonHelper

    private lateinit var cache: OrchestratorCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        // Mock prefs set and get behaviour
        val stringSlot = slot<String>()
        justRun { prefs.edit().putString(any(), capture(stringSlot)).commit() }
        every { prefs.getString(any(), any()) } answers { stringSlot.captured }

        cache = OrchestratorCache(
            securityManager,
            jsonHelper,
        )
    }

    @Test
    fun `Stores and restores steps`() {
        val expected = listOf(
            Step(
                id = StepId.FINGERPRINT_CAPTURE,
                navigationActionId = 3,
                destinationId = 4,
                payload = bundleOf("key" to "value"),
                status = StepStatus.COMPLETED,
                result = FingerprintCaptureResult(
                    "",
                    results = listOf(
                        FingerprintCaptureResult.Item(
                            captureEventId = GUID1,
                            identifier = IFingerIdentifier.LEFT_THUMB,
                            sample = null,
                        ),
                    ),
                ),
            ),
            Step(
                id = StepId.FACE_CAPTURE,
                navigationActionId = 5,
                destinationId = 6,
                payload = bundleOf("key" to "value"),
                status = StepStatus.COMPLETED,
                result = null,
            ),
        )

        cache.steps = expected
        val actual = cache.steps

        assertThat(actual).hasSize(2)
        compareStubs(expected[0], actual[0])
        compareStubs(expected[1], actual[1])
    }

    private fun compareStubs(
        expected: Step,
        actual: Step,
    ) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.navigationActionId).isEqualTo(expected.navigationActionId)
        assertThat(actual.destinationId).isEqualTo(expected.destinationId)
        assertThat(actual.status).isEqualTo(expected.status)
        assertThat(actual.result).isEqualTo(expected.result)

        // Direct comparison does not work with bundles due to implementation details
        assertThat(actual.payload.keySet()).isEqualTo(expected.payload.keySet())
    }
}
