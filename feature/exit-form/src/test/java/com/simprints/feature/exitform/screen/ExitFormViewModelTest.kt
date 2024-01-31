package com.simprints.feature.exitform.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.infra.events.EventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: EventRepository

    private lateinit var exitFormViewModel: ExitFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.nowTimestamp() } returns Timestamp(1L)

        exitFormViewModel = ExitFormViewModel(
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
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
    }

}
