package com.simprints.feature.exitform.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.exitform.ExitFormOption
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExitFormViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var exitFormViewModel: ExitFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(1L)

        exitFormViewModel = ExitFormViewModel(
            configManager,
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `show default options when configuration doesn't contain fingerprint`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf(GeneralConfiguration.Modality.FACE)
        }

        exitFormViewModel.start()

        assertThat(exitFormViewModel.visibleOptions.value).isEqualTo(DEFAULT_OPTIONS)
    }

    @Test
    fun `show scanner options when configuration contains fingerprint`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        }

        exitFormViewModel.start()

        val scannerOptions = DEFAULT_OPTIONS.toMutableSet()
        scannerOptions.remove(ExitFormOption.AppNotWorking)
        scannerOptions.add(ExitFormOption.ScannerNotWorking)

        assertThat(exitFormViewModel.visibleOptions.value).isEqualTo(scannerOptions)
    }

    @Test
    fun `show selectOption when back pressed and no option selected`() = runTest {
        exitFormViewModel.handleBackButton()

        assertThat(exitFormViewModel.requestSelectOptionEvent.value).isNotNull()
        assertThat(exitFormViewModel.requestFormSubmitEvent.value).isNull()
    }

    @Test
    fun `show submitForm when back pressed and option selected`() = runTest {
        exitFormViewModel.optionSelected(optionDoesNotRequiresInfo)
        exitFormViewModel.handleBackButton()

        assertThat(exitFormViewModel.requestSelectOptionEvent.value).isNull()
        assertThat(exitFormViewModel.requestFormSubmitEvent.value).isNotNull()
    }

    @Test
    fun `submit available when religious concern option selected`() = runTest {
        val submitEnabled = exitFormViewModel.submitEnabled.test()

        exitFormViewModel.optionSelected(optionDoesNotRequiresInfo)

        assertThat(exitFormViewModel.requestReasonEvent.value).isNull()
        assertThat(submitEnabled.valueHistory()).isEqualTo(listOf(false, true))
    }

    @Test
    fun `submit available when other option selected only after adding reason`() = runTest {
        val submitEnabled = exitFormViewModel.submitEnabled.test()

        exitFormViewModel.optionSelected(optionRequiresInfo)
        exitFormViewModel.reasonTextChanged("test")

        assertThat(exitFormViewModel.requestReasonEvent.value).isNotNull()
        assertThat(submitEnabled.valueHistory()).isEqualTo(listOf(false, false, true))
    }

    @Test
    fun `finish not called if no option selected`() {
        exitFormViewModel.submitClicked(null)

        assertThat(exitFormViewModel.finishEvent.value).isNull()
    }

    @Test
    fun `finish not called if selected option requires extra information`() {
        exitFormViewModel.optionSelected(optionRequiresInfo)
        exitFormViewModel.submitClicked(null)

        assertThat(exitFormViewModel.finishEvent.value).isNull()
    }

    @Test
    fun `finish called if selected option does not require extra information`() {
        exitFormViewModel.optionSelected(optionDoesNotRequiresInfo)
        exitFormViewModel.submitClicked(null)

        val finishedWith = exitFormViewModel.finishEvent.value?.peekContent()
        assertThat(finishedWith).isNotNull()
        assertThat(finishedWith?.first).isEqualTo(optionDoesNotRequiresInfo)
        assertThat(finishedWith?.second).isEmpty()
        coVerify { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `finish called if selected option does requires extra information`() {
        exitFormViewModel.optionSelected(optionRequiresInfo)
        exitFormViewModel.submitClicked("test")

        val finishedWith = exitFormViewModel.finishEvent.value?.peekContent()
        assertThat(finishedWith).isNotNull()
        assertThat(finishedWith?.first).isEqualTo(optionRequiresInfo)
        assertThat(finishedWith?.second).isEqualTo("test")
        coVerify { eventRepository.addOrUpdateEvent(any()) }
    }

    companion object {
        val optionDoesNotRequiresInfo = ExitFormOption.ReligiousConcerns
        val optionRequiresInfo = ExitFormOption.Other
        val DEFAULT_OPTIONS = setOf(
            ExitFormOption.ReligiousConcerns,
            ExitFormOption.DataConcerns,
            ExitFormOption.NoPermission,
            ExitFormOption.AppNotWorking,
            ExitFormOption.PersonNotPresent,
            ExitFormOption.TooYoung,
            ExitFormOption.WrongAgeGroupSelected,
            ExitFormOption.UncooperativeChild,
            ExitFormOption.Other,
        )
    }
}
