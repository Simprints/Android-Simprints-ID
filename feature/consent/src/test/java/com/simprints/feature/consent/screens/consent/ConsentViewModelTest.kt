package com.simprints.feature.consent.screens.consent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.SessionEventRepository
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
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var projectConfig: ProjectConfiguration


    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private val defaultModalityList = listOf(GeneralConfiguration.Modality.FACE)

    private lateinit var vm: ConsentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { configManager.getProjectConfiguration() } returns projectConfig
        every { projectConfig.consent } returns mockk()
        every { timeHelper.now() } returns TIMESTAMP

        vm = ConsentViewModel(
            timeHelper,
            configManager,
            eventRepository,
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

        coVerify { configManager.getProjectConfiguration() }

        assertThat(state.showLogo).isEqualTo(false)
        assertThat(state.consentTextBuilder).isNotNull()
        assertThat(state.showParentalConsent).isTrue()
        assertThat(state.parentalTextBuilder).isNotNull()
    }


    @Test
    fun `loadConfiguration passes correct values from config without parental consent to state`() = runTest {
        every { projectConfig.consent.allowParentalConsent } returns false
        every { projectConfig.general.modalities } returns defaultModalityList

        vm.loadConfiguration(ConsentType.ENROL)
        val state = vm.viewState.getOrAwaitValue()

        assertThat(state.showParentalConsent).isFalse()
        assertThat(state.parentalTextBuilder).isNull()
    }


    @Test
    fun `selected tab index is saved in the state`() = runTest {
        every { projectConfig.consent.allowParentalConsent } returns false
        every { projectConfig.general.modalities } returns defaultModalityList

        val selectedTabIndex = 3
        vm.setSelectedTab(selectedTabIndex)
        vm.loadConfiguration(ConsentType.ENROL)
        val state = vm.viewState.getOrAwaitValue()

        assertThat(state.selectedTab).isEqualTo(selectedTabIndex)
    }

    @Test
    fun `acceptClicked saves event and triggers correct return result`() {
        vm.acceptClicked(ConsentTab.INDIVIDUAL)
        val result = vm.returnConsentResult.getOrAwaitValue()

        val event = slot<ConsentEvent>()
        coVerify { eventRepository.addOrUpdateEvent(capture(event)) }
        with(event.captured) {
            assertThat(payload.consentType).isEqualTo(ConsentEvent.ConsentPayload.Type.INDIVIDUAL)
            assertThat(payload.result).isEqualTo(ConsentEvent.ConsentPayload.Result.ACCEPTED)
        }

        assertThat(result.peekContent()).isInstanceOf(ConsentResult::class.java)
        assertThat(vm.showExitForm.value).isNull()
    }

    @Test
    fun `declineClicked saves event`() {
        vm.declineClicked(ConsentTab.PARENTAL)

        val event = slot<ConsentEvent>()
        coVerify { eventRepository.addOrUpdateEvent(capture(event)) }
        with(event.captured) {
            assertThat(payload.consentType).isEqualTo(ConsentEvent.ConsentPayload.Type.PARENTAL)
            assertThat(payload.result).isEqualTo(ConsentEvent.ConsentPayload.Result.DECLINED)
        }
    }

    @Test
    fun `declineClicked triggers correct exit form for face only modality`() {
        every { projectConfig.general.modalities } returns listOf(GeneralConfiguration.Modality.FACE)

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_face)
    }

    @Test
    fun `declineClicked triggers correct exit form for fingerprint only modality`() {
        every { projectConfig.general.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_fingerprinting)
    }

    @Test
    fun `declineClicked triggers correct exit form for dual modality`() {
        every { projectConfig.general.modalities } returns listOf(
            GeneralConfiguration.Modality.FINGERPRINT,
            GeneralConfiguration.Modality.FACE,
        )

        vm.declineClicked(ConsentTab.PARENTAL)
        val result = vm.showExitForm.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()?.titleRes).isEqualTo(IDR.string.exit_form_title_biometrics)
    }

    @Test
    fun `handleExitFormResponse does nothing if exit form not submitted`() {
        vm.handleExitFormResponse(ExitFormResult(false))

        coVerify(exactly = 0) { eventRepository.removeLocationDataFromCurrentSession() }
        assertThat(vm.showExitForm.value).isNull()
    }

    @Test
    fun `handleExitFormResponse deletes location info if exit form submitted`() {
        vm.handleExitFormResponse(ExitFormResult(true))

        val result = vm.returnConsentResult.getOrAwaitValue()

        coVerify { eventRepository.removeLocationDataFromCurrentSession() }
        assertThat(result.getContentIfNotHandled()).isInstanceOf(ExitFormResult::class.java)
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
    }
}
