package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceConfigurationRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
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

    private val faceRequestFactory: FaceRequestFactory = FaceRequestFactoryImpl()
    private lateinit var faceStepProcess: FaceStepProcessor

    private lateinit var iFaceResponseMock: IFaceResponse

    val result by lazy {
        Intent().apply {
            putExtra(IFaceResponse.BUNDLE_KEY, iFaceResponseMock)
        }
    }

    @Before
    fun setUp() {
        faceStepProcess = FaceStepProcessorImpl(faceRequestFactory, configManager)
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

        verifyFaceIntent<FaceConfigurationRequest>(step, CONFIGURATION.value)
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
        faceStepProcess.processResult(CONFIGURATION.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
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
