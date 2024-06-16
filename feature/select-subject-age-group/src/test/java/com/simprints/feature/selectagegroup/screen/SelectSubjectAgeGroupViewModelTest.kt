package com.simprints.feature.selectagegroup.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.resources.R
import com.simprints.testtools.common.coroutines.TestCoroutineRule
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
    private lateinit var buildAgeGroups: BuildAgeGroupsDescriptionUseCase

    @RelaxedMockK
    private lateinit var configurationRepo: ConfigRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val ageGroupViewModels = listOf(
        AgeGroupDisplayModel("0-6 months", AgeGroup(0, 6)),
        AgeGroupDisplayModel("6-12 months", AgeGroup(6, 12)),
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { buildAgeGroups() } returns ageGroupViewModels

        viewModel = SelectSubjectAgeGroupViewModel(
            timeHelper,
            eventRepository,
            buildAgeGroups,
            configurationRepo,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
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
        val selectedAgeGroup = ageGroupViewModels.first().range
        viewModel.saveAgeGroupSelection(selectedAgeGroup)
        val result = viewModel.finish.test().value()
        Truth.assertThat(result.peekContent())
            .isEqualTo(selectedAgeGroup)

    }

    @Test
    fun `test onBackPressed fingerPrint modality`() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().general.modalities } returns listOf(
            GeneralConfiguration.Modality.FINGERPRINT
        )
        viewModel.onBackPressed()
        val result = viewModel.showExitForm.test().value().peekContent()

        // Assert that the titleRes and backButtonRes are equal to the fingerPrint modality
        Truth.assertThat(result.titleRes).isEqualTo(R.string.exit_form_title_fingerprinting)
        Truth.assertThat(result.backButtonRes).isEqualTo(R.string.exit_form_continue_fingerprints_button)
    }
    @Test
    fun `test onBackPressed face modality`() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().general.modalities } returns listOf(
            GeneralConfiguration.Modality.FACE
        )
        viewModel.onBackPressed()
        val result = viewModel.showExitForm.test().value().peekContent()

        // Assert that the titleRes and backButtonRes are equal to the face modality
        Truth.assertThat(result.titleRes).isEqualTo(R.string.exit_form_title_face)
        Truth.assertThat( result.backButtonRes).isEqualTo(R.string.exit_form_continue_face_button)
    }
    @Test
    fun `test onBackPressed multiple modalities`() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().general.modalities } returns listOf(
            GeneralConfiguration.Modality.FACE,
            GeneralConfiguration.Modality.FINGERPRINT
        )
        viewModel.onBackPressed()
        val result = viewModel.showExitForm.test().value().peekContent()

        // Assert that the titleRes and backButtonRes are equal to the biometrics modality
        Truth.assertThat(result.titleRes).isEqualTo(R.string.exit_form_title_biometrics)
        Truth.assertThat(result.backButtonRes).isEqualTo(R.string.exit_form_continue_fingerprints_button)
    }
}
