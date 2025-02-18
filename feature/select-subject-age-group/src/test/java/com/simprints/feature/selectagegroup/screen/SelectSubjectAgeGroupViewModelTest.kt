package com.simprints.feature.selectagegroup.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectSubjectAgeGroupViewModelTest {
    private lateinit var viewModel: SelectSubjectAgeGroupViewModel

    @RelaxedMockK
    private lateinit var timeHelper: TimeHelper

    @RelaxedMockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var buildAgeGroups: BuildAgeGroupsUseCase

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val ageGroupViewModels = listOf(
        AgeGroup(0, 6),
        AgeGroup(6, 12),
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { buildAgeGroups() } returns ageGroupViewModels

        viewModel = SelectSubjectAgeGroupViewModel(
            timeHelper,
            eventRepository,
            buildAgeGroups,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `test start`() = runTest {
        viewModel.start()
        val ageGroups = viewModel.ageGroups.test().value()

        Truth.assertThat(ageGroups.size).isEqualTo(ageGroupViewModels.size)
    }

    @Test
    fun `test saveAgeGroupSelection`() = runTest {
        viewModel.start()
        val selectedAgeGroup = ageGroupViewModels.first()
        viewModel.saveAgeGroupSelection(selectedAgeGroup)
        val result = viewModel.finish.test().value()
        Truth
            .assertThat(result.peekContent())
            .isEqualTo(selectedAgeGroup)
    }

    @Test
    fun `onBackPressed shows exit form`() = runTest {
        viewModel.onBackPressed()

        Truth.assertThat(viewModel.showExitForm.getOrAwaitValue()).isNotNull()
    }
}
