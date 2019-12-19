package com.simprints.id.orchestrator.steps

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
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
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class FingerprintStepProcessorImplTest : BaseStepProcessorTest() {

    private lateinit var preferencesManagerMock: PreferencesManager

    private val fingerprintRequestFactory: FingerprintRequestFactory = FingerprintRequestFactoryImpl()
    private lateinit var fingerprintStepProcess: FingerprintStepProcessor

    private val fingerprintResponseMock: IFingerprintResponse = mockk()
    val result = Intent().apply {
        putExtra(BUNDLE_KEY, fingerprintResponseMock)
    }

    @Before
    fun setUp() {
        preferencesManagerMock = mockk()

        with(preferencesManagerMock) {
            every { language } returns "en"
            every { fingerStatus } returns emptyMap()
            every { logoExists } returns true
            every { organizationName } returns "some_org"
            every { programName } returns "some_name"
            every { matchGroup } returns GROUP.GLOBAL
        }

        fingerprintStepProcess = FingerprintStepProcessorImpl(
            fingerprintRequestFactory,
            preferencesManagerMock
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
    fun stepProcessorShouldBuildTheRightStepForCapturing() {
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

    @After
    fun tearDown() {
        stopKoin()
    }
}
