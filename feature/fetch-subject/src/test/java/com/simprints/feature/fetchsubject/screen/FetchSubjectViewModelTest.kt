package com.simprints.feature.fetchsubject.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.fetchsubject.screen.usecase.FetchSubjectUseCase
import com.simprints.feature.fetchsubject.screen.usecase.SaveSubjectFetchEventUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class FetchSubjectViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var fetchSubjectUseCase: FetchSubjectUseCase

    @MockK
    lateinit var saveSubjectFetchEventUseCase: SaveSubjectFetchEventUseCase

    private lateinit var viewModel: FetchSubjectViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns TIMESTAMP

        viewModel = FetchSubjectViewModel(
            timeHelper,
            fetchSubjectUseCase,
            saveSubjectFetchEventUseCase,
        )
    }

    @Test
    fun `tries fetching subject`() = runTest {
        coEvery { fetchSubjectUseCase.invoke(any(), any()) } returns FetchSubjectState.NotFound

        viewModel.fetchSubject(PROJECT_ID, SUBJECT_ID)
        val result = viewModel.subjectState.test()

        assertThat(result.value().getContentIfNotHandled()).isNotNull()
        coVerify { fetchSubjectUseCase.invoke(any(), any()) }
    }

    @Test
    fun `onViewCreated tries fetching subject when it wasn't attempted yet`() = runTest {
        coEvery { fetchSubjectUseCase.invoke(any(), any()) } returns FetchSubjectState.NotFound

        viewModel.onViewCreated(PROJECT_ID, SUBJECT_ID)
        val result = viewModel.subjectState.test()

        assertThat(result.value().getContentIfNotHandled()).isNotNull()
        coVerify { fetchSubjectUseCase.invoke(any(), any()) }
    }

    @Test
    fun `onViewCreated doesn't try fetching subject when it was already attempted`() = runTest {
        coEvery { fetchSubjectUseCase.invoke(any(), any()) } returns FetchSubjectState.NotFound

        viewModel.onViewCreated(PROJECT_ID, SUBJECT_ID)
        viewModel.onViewCreated(PROJECT_ID, SUBJECT_ID)

        coVerify(exactly = 1) { fetchSubjectUseCase.invoke(any(), any()) }
    }

    @Test
    fun `saves event after fetching subject`() = runTest {
        coEvery { fetchSubjectUseCase.invoke(any(), any()) } returns FetchSubjectState.NotFound

        viewModel.fetchSubject(PROJECT_ID, SUBJECT_ID)

        coVerify { saveSubjectFetchEventUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun `startExitForm returns ShowExitForm`() {
        viewModel.startExitForm()
        val result = viewModel.subjectState.getOrAwaitValue()

        assertThat(result.peekContent()).isInstanceOf(FetchSubjectState.ShowExitForm::class.java)
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "subjectId"
    }
}
