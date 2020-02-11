package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.MATCH
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.moduleapi.face.responses.IFaceResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class FaceStepProcessorImplTest : BaseStepProcessorTest() {

    @MockK lateinit var preferencesManagerMock: PreferencesManager

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
        MockKAnnotations.init(this, relaxed = true)
        with(preferencesManagerMock) {
            every { this@with.language } returns "en"
            every { this@with.fingerStatus } returns emptyMap()
            every { this@with.logoExists } returns true
            every { this@with.organizationName } returns "some_org"
            every { this@with.programName } returns "some_name"
        }

        faceStepProcess = FaceStepProcessorImpl(faceRequestFactory)


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
    fun stepProcessorShouldBuildTheRightStepForEnrol() {
        val step = faceStepProcess.buildCaptureStep()

        verifyFaceIntent<FaceCaptureRequest>(step, CAPTURE.value)
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() {
        val step = faceStepProcess.buildStepMatch(mockk(), mockk())

        verifyFaceIntent<FaceMatchRequest>(step, MATCH.value)
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
    fun stepProcessorShouldNotProcessNoFaceResult() {
        faceStepProcess.processResult(0, Activity.RESULT_OK, result)

        verify(exactly = 0) { iFaceResponseMock.fromModuleApiToDomain() }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
