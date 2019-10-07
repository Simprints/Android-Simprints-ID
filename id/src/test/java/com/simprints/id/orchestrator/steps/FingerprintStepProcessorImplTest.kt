package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.ModuleApiToDomainFingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.*
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.orchestrator.verifyAppRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY
import com.simprints.testtools.common.syntax.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class FingerprintStepProcessorImplTest : BaseStepProcessorTest() {

    private lateinit var preferencesManagerMock: PreferencesManager
    private lateinit var converterModuleApiToDomainMock: ModuleApiToDomainFingerprintResponse

    private val fingerprintRequestFactory: FingerprintRequestFactory = FingerprintRequestFactoryImpl()
    private lateinit var fingerprintStepProcess: FingerprintStepProcessor

    val result = Intent().apply {
        putExtra(BUNDLE_KEY, mock<IFingerprintResponse>())
    }

    @Before
    fun setUp() {
        preferencesManagerMock = mock()
        converterModuleApiToDomainMock = mock()

        with(preferencesManagerMock) {
            whenever(language) thenReturn "en"
            whenever(fingerStatus) thenReturn emptyMap()
            whenever(logoExists) thenReturn true
            whenever(organizationName) thenReturn "some_org"
            whenever(programName) thenReturn "some_name"
            whenever(matchGroup) thenReturn GROUP.GLOBAL
        }

        fingerprintStepProcess = FingerprintStepProcessorImpl(
            fingerprintRequestFactory,
            converterModuleApiToDomainMock,
            preferencesManagerMock
        )
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForVerify() {
        with(verifyAppRequest) {
            val step = fingerprintStepProcess.buildStepVerify(projectId, userId, moduleId, metadata, verifyGuid)

            verifyFingerprintIntent<FingerprintVerifyRequest>(step, VERIFY.value)
        }
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForEnrol() {
        with(enrolAppRequest) {
            val step = fingerprintStepProcess.buildStepEnrol(projectId, userId, moduleId, metadata)

            verifyFingerprintIntent<FingerprintEnrolRequest>(step, ENROL.value)
        }
    }

    @Test
    fun stepProcessorShouldBuildTheRightStepForIdentify() {
        with(identifyAppRequest) {
            val step = fingerprintStepProcess.buildStepIdentify(projectId, userId, moduleId, metadata)

            verifyFingerprintIntent<FingerprintIdentifyRequest>(step, IDENTIFY.value)
        }
    }

    @Test
    fun stepProcessorShouldProcessFingerprintEnrolResult() {
        fingerprintStepProcess.processResult(ENROL.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFingerprintResponse(anyNotNull()) }
    }

    @Test
    fun stepProcessorShouldProcessFingerprintIdentifyResult() {
        fingerprintStepProcess.processResult(IDENTIFY.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFingerprintResponse(anyNotNull()) }
    }

    @Test
    fun stepProcessorShouldProcessFingerprintVerifyResult() {
        fingerprintStepProcess.processResult(VERIFY.value, Activity.RESULT_OK, result)

        verifyOnce(converterModuleApiToDomainMock) { fromModuleApiToDomainFingerprintResponse(anyNotNull()) }
    }

    @Test
    fun stepProcessorShouldNotProcessNoFingerprintResult() {
        fingerprintStepProcess.processResult(0, Activity.RESULT_OK, result)

        verifyNever(converterModuleApiToDomainMock) { fromModuleApiToDomainFingerprintResponse(anyNotNull()) }
    }

    @After
    fun tearDown(){
        stopKoin()
    }
}
