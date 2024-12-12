package com.simprints.feature.selectsubject.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SelectSubjectViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var viewModel: SelectSubjectViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns TIMESTAMP

        viewModel = SelectSubjectViewModel(
            timeHelper,
            authStore,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `saves selection if signed in`() = runTest {
        every { authStore.isProjectIdSignedIn(any()) } returns true

        viewModel.saveGuidSelection(PROJECT_ID, SUBJECT_ID)
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(result).isTrue()
        coVerify { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `does not save selection if not signed in`() = runTest {
        every { authStore.isProjectIdSignedIn(any()) } returns false

        viewModel.saveGuidSelection(PROJECT_ID, SUBJECT_ID)
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(result).isFalse()
        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `correctly handles exception with saving`() = runTest {
        every { authStore.isProjectIdSignedIn(any()) } returns true
        coEvery { eventRepository.addOrUpdateEvent(any()) } throws Exception()

        viewModel.saveGuidSelection(PROJECT_ID, SUBJECT_ID)
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(result).isFalse()
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "subjectId"
    }
}
