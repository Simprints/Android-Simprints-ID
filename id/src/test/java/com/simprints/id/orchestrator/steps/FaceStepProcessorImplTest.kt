package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.verifyAppRequest
import com.simprints.id.testtools.TestApplication
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class FaceStepProcessorImplTest : BaseStepProcessorTest() {

    @Mock lateinit var preferencesManagerMock: PreferencesManager

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
        MockitoAnnotations.initMocks(this)
        with(preferencesManagerMock) {
            whenever(language) thenReturn "en"
            whenever(fingerStatus) thenReturn emptyMap()
            whenever(logoExists) thenReturn true
            whenever(organizationName) thenReturn "some_org"
            whenever(programName) thenReturn "some_name"
        }

        faceStepProcess = FaceStepProcessorImpl(faceRequestFactory)


        mockFromModuleApiToDomainExt()
    }

    private fun mockFromModuleApiToDomainExt() {
        mockkStatic("com.simprints.id.domain.moduleapi.face.responses.FaceResponseKt")
        iFaceResponseMock = mockk()
        every {
            iFaceResponseMock.fromModuleApiToDomain()
        } returns mock()
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForVerify() {
        with(verifyAppRequest) {
            val step = faceStepProcess.buildStepVerify(projectId, userId, moduleId)

            verifyFaceIntent<FaceVerifyRequest>(step, VERIFY.value)
        }
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForEnrol() {
        val step = faceStepProcess.buildCaptureStep()

        verifyFaceIntent<FaceCaptureRequest>(step, CAPTURE.value)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() {
        with(identifyAppRequest) {
            val step = faceStepProcess.buildStepIdentify(projectId, userId, moduleId)

            verifyFaceIntent<FaceIdentifyRequest>(step, IDENTIFY.value)
        }
    }

    @Test
    fun stepProcessorShouldProcessFaceEnrolResult() {
        faceStepProcess.processResult(CAPTURE.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessFaceIdentifyResult() {
        faceStepProcess.processResult(IDENTIFY.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldProcessFaceVerifyResult() {
        faceStepProcess.processResult(VERIFY.value, Activity.RESULT_OK, result)

        verify(exactly = 1) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @Test
    fun stepProcessorShouldNotProcessNoFaceResult() {
        faceStepProcess.processResult(0, Activity.RESULT_OK, result)

        verify(exactly = 0) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
