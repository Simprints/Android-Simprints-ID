package com.simprints.feature.consent.screens.consent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.consent.screens.consent.helpers.GeneralConsentTextHelper
import com.simprints.feature.consent.screens.consent.helpers.ParentalConsentTextHelper
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.ConsentEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.simprints.infra.resources.R as IDR


class ConsentViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var generalConsentTextHelper: GeneralConsentTextHelper

    @MockK
    private lateinit var parentalConsentTextHelper: ParentalConsentTextHelper

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var projectConfig: ProjectConfiguration


    @MockK
    private lateinit var eventRepository: EventRepository

    private val defaultModalityList = listOf(GeneralConfiguration.Modality.FACE)

    private lateinit var vm: ConsentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        every { projectConfig.consent } returns mockk()

        every { timeHelper.now() } returns TIMESTAMP
        every { generalConsentTextHelper.assembleText(any(), any(), any()) } returns GENERAL_CONSENT
        every { parentalConsentTextHelper.assembleText(any(), any(), any()) } returns PARENTAL_CONSENT

        vm = ConsentViewModel(
            timeHelper,
            configRepository,
            eventRepository,
            generalConsentTextHelper,
            parentalConsentTextHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
    }

    @Test
    fun `loadConfiguration passes correct values from basic config to state`() = runTest {
        every { projectConfig.consent.displaySimprintsLogo } returns false
        every { projectConfig.consent.allowParentalConsent } returns true
        every { projectConfig.general.modalities } returns defaultModalityList

        vm.loadConfiguration(ConsentType.ENROL)
        val state = vm.viewState.getOrAwaitValue()

        coVerify { configRepository.getProjectConfiguration() }
        verify { generalConsentTextHelper.assembleText(any(), eq(defaultModalityList), eq(ConsentType.ENROL)) }
        verify { parentalConsentTextHelper.assembleText(any(), eq(defaultModalityList), eq(ConsentType.ENROL)) }

        Truth.assertThat(state.showLogo).isEqualTo(false)
        Truth.assertThat(state.consentText).isEqualTo(GENERAL_CONSENT)
        Truth.assertThat(state.showParentalConsent).isTrue()
        Truth.assertThat(state.parentalConsentText).isEqualTo(PARENTAL_CONSENT)
    }


    @Test
    fun `loadConfiguration passes correct values from config without parental consent to state`() = runTest {
        every { projectConfig.consent.allowParentalConsent } returns false
        every { projectConfig.general.modalities } returns defaultModalityList

        vm.loadConfiguration(ConsentType.ENROL)
        val state = vm.viewState.getOrAwaitValue()

        verify(exactly = 0) { parentalConsentTextHelper.assembleText(any(), any(), any()) }
        Truth.assertThat(state.showParentalConsent).isFalse()
        Truth.assertThat(state.parentalConsentText).isEmpty()
    }

    @Test
    fun `acceptClicked saves event and triggers correct return result`() {
        vm.acceptClicked(ConsentTab.INDIVIDUAL)
        val result = vm.returnConsentResult.getOrAwaitValue()

        val event = slot<ConsentEvent>()
        coVerify { eventRepository.addOrUpdateEvent(capture(event)) }
        with(event.captured) {
            Truth.assertThat(payload.consentType).isEqualTo(ConsentEvent.ConsentPayload.Type.INDIVIDUAL)
            Truth.assertThat(payload.result).isEqualTo(ConsentEvent.ConsentPayload.Result.ACCEPTED)
        }

        Truth.assertThat(result.peekContent()).isInstanceOf(ConsentResult::class.java)
        Truth.assertThat(vm.showExitForm.value).isNull()
    }

    @Test
    fun `declineClicked saves event`() {
        vm.declineClicked(ConsentTab.PARENTAL)

        val event = slot<ConsentEvent>()
        coVerify { eventRepository.addOrUpdateEvent(capture(event)) }
        with(event.captured) {
            Truth.assertThat(payload.consentType).isEqualTo(ConsentEvent.ConsentPayload.Type.PARENTAL)
            Truth.assertThat(payload.result).isEqualTo(ConsentEvent.ConsentPayload.Result.DECLINED)
        }
    }

    @Test
    fun `declineClicked triggers correct exit form for face only modality`() {
        every { projectConfig.general.modalities } returns listOf(GeneralConfiguration.Modality.FACE)

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        Truth.assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_face)
    }

    @Test
    fun `declineClicked triggers correct exit form for fingerprint only modality`() {
        every { projectConfig.general.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        Truth.assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_fingerprinting)
    }

    @Test
    fun `declineClicked triggers correct exit form for dual modality`() {
        every { projectConfig.general.modalities } returns listOf(
            GeneralConfiguration.Modality.FINGERPRINT,
            GeneralConfiguration.Modality.FACE,
        )

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        Truth.assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_biometrics)
    }

    @Test
    fun `handleExitFormResponse does nothing if exit form not submitted`() {
        vm.handleExitFormResponse(ExitFormResult(false))

        coVerify(exactly = 0) { eventRepository.removeLocationDataFromCurrentSession() }
        Truth.assertThat(vm.showExitForm.value).isNull()
    }

    @Test
    fun `handleExitFormResponse deletes location info if exit form submitted`() {
        vm.handleExitFormResponse(ExitFormResult(true))

        val result = vm.returnConsentResult.getOrAwaitValue()

        coVerify { eventRepository.removeLocationDataFromCurrentSession() }
        Truth.assertThat(result.getContentIfNotHandled()).isInstanceOf(ExitFormResult::class.java)
    }

    companion object {
        private const val TIMESTAMP = 1L
        private const val GENERAL_CONSENT = "General consent"
        private const val PARENTAL_CONSENT = "Parental consent"
    }
}
