package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowProvider
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.store.models.Finger
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchContract
import com.simprints.infra.config.sync.ConfigManager
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

    private lateinit var fingerprintStepProcess: FingerprintStepProcessor

    private val fingerprintResponseMock: IFingerprintResponse = mockk()
    val result = Intent().apply {
        putExtra(BUNDLE_KEY, fingerprintResponseMock)
    }

    @Before
    fun setUp() {
        fingerprintStepProcess = FingerprintStepProcessorImpl(configManager)
        mockFromModuleApiToDomainExt()
    }

    private fun mockFromModuleApiToDomainExt() {
        mockkStatic("com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponseKt")
        every { fingerprintResponseMock.fromModuleApiToDomain() } returns mockk()
    }


    @Test
    fun stepProcessorShouldBuildTheRightStepForMatching() {
        val step = fingerprintStepProcess.buildStepToMatch(
            listOf(FingerprintCaptureSample(Finger.LEFT_3RD_FINGER, ByteArray(0), 0, "", null)),
            mockk(),
            mockk()
        )
        verifyFingerprintMatcherIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForCapturing() = runTest {
        val step = fingerprintStepProcess.buildStepToCapture(FlowProvider.FlowType.ENROL)
        verifyFingerprintCaptureIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldProcessFingerprintEnrolResult() {
        val captureResult = Intent().putExtra(FingerprintCaptureContract.RESULT, FingerprintCaptureResult(emptyList()))
        val result = fingerprintStepProcess.processResult(FingerprintRequestCode.CAPTURE.value, Activity.RESULT_OK, captureResult)

        assertThat(result).isInstanceOf(FingerprintCaptureResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessFingerprintMatchResult() {
        val matchResult = Intent().putExtra(MatchContract.RESULT, FingerprintMatchResult(emptyList()))
        val result = fingerprintStepProcess.processResult(FingerprintRequestCode.MATCH.value, Activity.RESULT_OK, matchResult)

        assertThat(result).isInstanceOf(FingerprintMatchResponse::class.java)
    }

    @Test
    fun stepProcessorShouldNotProcessMissingMatchResult() {
        val matchResult = Intent().putExtra(MatchContract.RESULT, Bundle())
        val result = fingerprintStepProcess.processResult(FingerprintRequestCode.MATCH.value, Activity.RESULT_OK, matchResult)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessorShouldNotProcessNoFingerprintResult() {
        fingerprintStepProcess.processResult(0, Activity.RESULT_OK, result)

        verify(exactly = 0) { fingerprintResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldNotProcessNullResult() {
        val matchResult = Intent()
        val result = fingerprintStepProcess.processResult(FingerprintRequestCode.MATCH.value, Activity.RESULT_OK, matchResult)

        assertThat(result).isNull()
    }
}
