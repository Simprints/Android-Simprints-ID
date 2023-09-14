package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.ConfigManager
import com.simprints.moduleapi.face.responses.IFaceResponse
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

    private lateinit var iFaceResponseMock: IFaceResponse

    val result by lazy {
        Intent().apply {
            putExtra(IFaceResponse.BUNDLE_KEY, iFaceResponseMock)
        }
    }

    @Before
    fun setUp() {
        faceStepProcess = FaceStepProcessorImpl(configManager)
        mockFromModuleApiToDomainExt()
    }

    private fun mockFromModuleApiToDomainExt() {
        mockkStatic("com.simprints.id.domain.moduleapi.face.responses.FaceResponseKt")
        iFaceResponseMock = mockk()
        every {
            iFaceResponseMock.fromModuleApiToDomain()
        } returns mockk()
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForVerify() {
        val step = faceStepProcess.buildStepMatch(mockk(), mockk())

        verifyFaceIntent<FaceMatchRequest>(step, MATCH.value)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForEnrol() = runTest {
        val step = faceStepProcess.buildCaptureStep()

        verifyFaceIntent<FaceCaptureRequest>(step, CAPTURE.value)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() = runTest {
        val step = faceStepProcess.buildStepMatch(mockk(), mockk())

        verifyFaceIntent<FaceMatchRequest>(step, MATCH.value)
    }

    @Test
    fun stepProcessorShouldBuildRightStepForConfiguration() {
        val step = faceStepProcess.buildConfigurationStep("projectId", "deviceId")

        verifyFaceConfigurationIntent<Bundle>(step, CONFIGURATION.value)
    }

    @Test
    fun stepProcessorShouldProcessFaceEnrolResult() {
        faceStepProcess.processResult(CAPTURE.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessFaceIdentifyResult() {
        faceStepProcess.processResult(MATCH.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessFaceVerifyResult() {
        faceStepProcess.processResult(MATCH.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessConfigurationResult() {
        val configurationResult = Intent().putExtra(FaceConfigurationContract.RESULT, FaceConfigurationResult(true))
        val result = faceStepProcess.processResult(CONFIGURATION.value, Activity.RESULT_OK, configurationResult)

        assertThat(result).isInstanceOf(FaceConfigurationResponse::class.java)
    }

    @Test
    fun stepProcessorShouldNotProcessNoFaceResult() {
        faceStepProcess.processResult(0, Activity.RESULT_OK, result)

        verify(exactly = 0) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
