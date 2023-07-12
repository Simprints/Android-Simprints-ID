package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.MATCH
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.ConfigManager
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class FingerprintStepProcessorImplTest : BaseStepProcessorTest() {

    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { fingersToCapture } returns listOf()
            }
        }
    }

    private val fingerprintRequestFactory: FingerprintRequestFactory =
        FingerprintRequestFactoryImpl()
    private lateinit var fingerprintStepProcess: FingerprintStepProcessor

    private val fingerprintResponseMock: IFingerprintResponse = mockk()
    val result = Intent().apply {
        putExtra(BUNDLE_KEY, fingerprintResponseMock)
    }

    @Before
    fun setUp() {
        fingerprintStepProcess = FingerprintStepProcessorImpl(
            fingerprintRequestFactory,
            configManager
        )
        mockFromModuleApiToDomainExt()
    }

    private fun mockFromModuleApiToDomainExt() {
        mockkStatic("com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponseKt")
        every { fingerprintResponseMock.fromModuleApiToDomain() } returns mockk()
    }


    @Test
    fun stepProcessorShouldBuildTheRightStepForMatching() {
        val step = fingerprintStepProcess.buildStepToMatch(mockk(), mockk())
        verifyFingerprintIntent<FingerprintMatchRequest>(step, MATCH.value)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForCapturing() = runTest {
        with(enrolAppRequest) {
            val step = fingerprintStepProcess.buildStepToCapture()
            verifyFingerprintIntent<FingerprintCaptureRequest>(step, CAPTURE.value)
        }
    }

    @Test
    fun stepProcessorShouldProcessFingerprintEnrolResult() {
        fingerprintStepProcess.processResult(CAPTURE.value, Activity.RESULT_OK, result)

        verify { fingerprintResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessFingerprintMatchResult() {
        fingerprintStepProcess.processResult(MATCH.value, Activity.RESULT_OK, result)

        verify { fingerprintResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldNotProcessNoFingerprintResult() {
        fingerprintStepProcess.processResult(0, Activity.RESULT_OK, result)

        verify(exactly = 0) { fingerprintResponseMock.fromModuleApiToDomain() }
    }
}
