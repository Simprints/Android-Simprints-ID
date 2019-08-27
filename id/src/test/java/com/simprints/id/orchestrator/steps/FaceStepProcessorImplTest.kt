package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.verifyAppRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.testtools.common.syntax.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class FaceStepProcessorImplTest : BaseStepProcessorTest() {

    @Mock lateinit var preferencesManagerMock: PreferencesManager
    @Mock lateinit var converterModuleApiToDomainMock: ModuleApiToDomainFaceResponse

    private val faceRequestFactory: FaceRequestFactory = FaceRequestFactoryImpl()
    private lateinit var faceStepProcess: FaceStepProcessor
    val result = Intent().apply {
        putExtra(IFaceResponse.BUNDLE_KEY, mock<IFaceResponse>())
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

        faceStepProcess = FaceStepProcessorImpl(faceRequestFactory, converterModuleApiToDomainMock)
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
        with(enrolAppRequest) {
            val step = faceStepProcess.buildStepEnrol(projectId, userId, moduleId)

            verifyFaceIntent<FaceEnrolRequest>(step, ENROL.value)
        }
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() {
        with(identifyAppRequest) {
            val step = faceStepProcess.buildStepIdentify(projectId, userId, moduleId)

            verifyFaceIntent<FaceIdentifyRequest>(step, IDENTIFY.value)
        }
    }

    @Test
    fun steProcessorShouldProcessFaceEnrolResult() {
        faceStepProcess.processResult(ENROL.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFaceResponse(anyNotNull()) }
    }

    @Test
    fun steProcessorShouldProcessFaceIdentifyResult() {
        faceStepProcess.processResult(IDENTIFY.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFaceResponse(anyNotNull()) }
    }

    @Test
    fun steProcessorShouldProcessFaceVerifyResult() {
        faceStepProcess.processResult(VERIFY.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFaceResponse(anyNotNull()) }
    }

    @Test
    fun steProcessorShouldNotProcessNoFaceResult() {
        faceStepProcess.processResult(0, Activity.RESULT_OK, result)

        verifyNever(converterModuleApiToDomainMock) { fromModuleApiToDomainFaceResponse(anyNotNull()) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
