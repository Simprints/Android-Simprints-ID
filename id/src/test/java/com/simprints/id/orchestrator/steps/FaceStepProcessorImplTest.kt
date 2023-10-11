package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.matcher.MatchContract
import com.simprints.matcher.FaceMatchResult
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.ConfigManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class FaceStepProcessorImplTest : BaseStepProcessorTest() {

    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { face } returns mockk {
                every { nbOfImagesToCapture } returns 2
            }
        }
    }

    private lateinit var faceStepProcess: FaceStepProcessor

    @Before
    fun setUp() {
        faceStepProcess = FaceStepProcessorImpl(configManager)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForVerify() {
        val step = faceStepProcess.buildStepMatch(mockk(), mockk(), mockk())

        verifyFaceMatcherIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForEnrol() = runTest {
        val step = faceStepProcess.buildCaptureStep()

        verifyFaceCaptureIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() = runTest {
        val step = faceStepProcess.buildStepMatch(mockk(), mockk(), mockk())

        verifyFaceMatcherIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldBuildRightStepForConfiguration() {
        val step = faceStepProcess.buildConfigurationStep("projectId", "deviceId")

        verifyFaceConfigurationIntent<Bundle>(step)
    }

    @Test
    fun stepProcessorShouldProcessFaceEnrolResult() {
        val captureResult = Intent().putExtra(FaceCaptureContract.RESULT, FaceCaptureResult(emptyList()))
        val result = faceStepProcess.processResult(CAPTURE.value, Activity.RESULT_OK, captureResult)

        assertThat(result).isInstanceOf(FaceCaptureResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessFaceIdentifyResult() {
        val matchResult = Intent().putExtra(MatchContract.RESULT, FaceMatchResult(emptyList()))
        val result = faceStepProcess.processResult(MATCH.value, Activity.RESULT_OK, matchResult)

        assertThat(result).isInstanceOf(FaceMatchResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessFaceVerifyResult() {
        val matchResult = Intent().putExtra(MatchContract.RESULT, FaceMatchResult(emptyList()))
        val result = faceStepProcess.processResult(MATCH.value, Activity.RESULT_OK, matchResult)

        assertThat(result).isInstanceOf(FaceMatchResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessConfigurationResult() {
        val configurationResult = Intent().putExtra(FaceConfigurationContract.RESULT, FaceConfigurationResult(true))
        val result = faceStepProcess.processResult(CONFIGURATION.value, Activity.RESULT_OK, configurationResult)

        assertThat(result).isInstanceOf(FaceConfigurationResponse::class.java)
    }

    @Test
    fun stepProcessorShouldNotProcessNoFaceResult() {
        val result = faceStepProcess.processResult(0, Activity.RESULT_OK, Intent())

        assertThat(result).isNull()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
