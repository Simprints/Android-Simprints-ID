package com.simprints.id.orchestrator.steps

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.setup.SetupContract
import com.simprints.feature.setup.SetupResult
import com.simprints.id.exitformhandler.ExitFormReason
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.core.response.*
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CoreStepProcessorImplTest : BaseStepProcessorTest() {

    @MockK
    private lateinit var mapStepsForLastBiometricEnrol: MapStepsForLastBiometricEnrolUseCase

    private lateinit var coreStepProcessor: CoreStepProcessorImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coreStepProcessor = CoreStepProcessorImpl(mapStepsForLastBiometricEnrol)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForSetup() {
        val step = coreStepProcessor.buildStepSetup()

        verifySetupIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForEnrol() {
        val step = coreStepProcessor.buildStepConsent(ConsentType.ENROL)

        verifyConsentIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForIdentify() {
        val step = coreStepProcessor.buildStepConsent(ConsentType.IDENTIFY)

        verifyConsentIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForVerify() {
        val step = coreStepProcessor.buildStepConsent(ConsentType.VERIFY)

        verifyConsentIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForGuidFetch() {
        val step = coreStepProcessor.buildFetchGuidStep(DEFAULT_PROJECT_ID, GUID1)

        verifyFetchGuidIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForGuidSelect() {
        val step = coreStepProcessor.buildConfirmIdentityStep(DEFAULT_PROJECT_ID, GUID1)

        verifyGuidSelectedIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForEnrolLastBiometric() {
        every { mapStepsForLastBiometricEnrol.invoke(any()) } returns emptyList()

        val step = coreStepProcessor.buildAppEnrolLastBiometricsStep(
            DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, emptyList(), GUID1
        )

        verifyLastBiometricIntent<Bundle>(step)
    }

    @Test
    fun stepProcessor_shouldSkipLegacyNavigationConsentResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldSkipLegacyFetchGuidResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, FetchGUIDResponse(false))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldSkipLegacySelectGuidResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, GuidSelectionResponse(true))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldSkipLegacyEnrolLastBiometricResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, EnrolLastBiometricsResponse(null))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldSkipLegacySetupResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, SetupResponse(true))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldReturnConsentResultWhenAccepted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ConsentResult(true))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(AskConsentResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldReturnConsentResultWhenExitFormSubmitted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ExitFormResult(true, ExitFormOption.Other))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(ExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFetchGUIDResponseWhenReturnedToExitForm() {
        val fetchActivityReturn: Intent = Intent().putExtra(FetchSubjectContract.FETCH_SUBJECT_RESULT, ExitFormResult(true, ExitFormOption.Other))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(ExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFetchGUIDResponseNotFoundOnlineWhenTried() {
        val fetchActivityReturn: Intent = Intent().putExtra(FetchSubjectContract.FETCH_SUBJECT_RESULT, FetchSubjectResult(
            found = false,
            wasOnline = true
        ))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(FetchGUIDResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFetchGUIDResponseNotFoundOfflineWhenTried() {
        val fetchActivityReturn: Intent = Intent().putExtra(FetchSubjectContract.FETCH_SUBJECT_RESULT, FetchSubjectResult(
            found = false,
            wasOnline = false
        ))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(FetchGUIDResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFetchGUIDResponseFoundWhenTried() {
        val fetchActivityReturn: Intent = Intent().putExtra(FetchSubjectContract.FETCH_SUBJECT_RESULT, FetchSubjectResult(
            found = true
        ))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(FetchGUIDResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldReturnNullWhenExitFormNotSubmitted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ExitFormResult(false))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldProcessSelectedGuidResponse() {
        val consentData = Intent().putExtra(SelectSubjectContract.SELECT_SUBJECT_RESULT, SelectSubjectResult(true))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(GuidSelectionResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessEnrolLastBiometricResponse() {
        val consentData = Intent().putExtra(EnrolLastBiometricContract.ENROL_LAST_RESULT, EnrolLastBiometricResult(null))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(EnrolLastBiometricsResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessCoreExitFormResult() {
        val exitFormData = Intent().putExtra(
            CORE_STEP_BUNDLE,
            ExitFormResponse(ExitFormReason.OTHER, "optional_text")
        )
        val result = coreStepProcessor.processResult(exitFormData)

        assertThat(result).isInstanceOf(ExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessResultFromSetup() {
        val setupData = Intent().putExtra(SetupContract.SETUP_RESULT, SetupResult(true))
        val result = coreStepProcessor.processResult(setupData)

        assertThat(result).isInstanceOf(SetupResponse::class.java)
    }


    @Test
    fun stepProcessor_shouldReturnNullWhenNoResponseInData() {
        val setupData = Intent().putExtra("invalidKey", "invalidData")
        val result = coreStepProcessor.processResult(setupData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldReturnNullWhenNoValidResponseInData() {
        val result = coreStepProcessor.processResult(Intent())

        assertThat(result).isNull()
    }

    companion object {
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}
