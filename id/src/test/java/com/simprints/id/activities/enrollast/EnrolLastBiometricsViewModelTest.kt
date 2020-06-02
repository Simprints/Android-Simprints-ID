package com.simprints.id.activities.enrollast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.SOME_GUID
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnrolLastBiometricsViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var viewModel: EnrolLastBiometricsViewModel
    private val stepsWithLastEnrolBiometrics = listOf(
        Step(SOME_GUID, 0, "activity_name", "key", mockk<EnrolLastBiometricsRequest>(), EnrolLastBiometricsResponse(SOME_GUID), COMPLETED))

    private val appRequestWithPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, stepsWithLastEnrolBiometrics, SOME_GUID)
    private val appRequestWithoutPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, emptyList(), SOME_GUID)

    @MockK lateinit var timeHelper: TimeHelper
    @MockK lateinit var enrolHelper: EnrolmentHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = EnrolLastBiometricsViewModel(enrolHelper, timeHelper)
    }

    @Test
    fun getNextStep_enrolFails_shouldProduceFailedState() {
        runBlocking {
            val request = mockk<EnrolLastBiometricsRequest>()
            every { request.previousSteps } throws Throwable("No steps from previous run")
            viewModel.processEnrolLastBiometricsRequest(request)
            Truth.assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
        }
    }

    @Test
    fun getNextStep_enrolAlreadyHappened_shouldProduceSuccessState() {
        runBlocking {
            viewModel.processEnrolLastBiometricsRequest(appRequestWithPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                Truth.assertThat(this.value).isInstanceOf(Success::class.java)
                Truth.assertThat((this.value as Success).newGuid).isEqualTo(SOME_GUID)
            }
        }
    }

    @Test
    fun getNextStep_enrolNeverHappened_shouldProduceSuccessState() {
        runBlocking {
            val newEnrolment = PeopleGeneratorUtils.getRandomPerson()
            every { enrolHelper.buildPerson(any(), any(), any(), any(), any(), any()) } returns newEnrolment

            viewModel.processEnrolLastBiometricsRequest(appRequestWithoutPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                Truth.assertThat(this.value).isInstanceOf(Success::class.java)
                Truth.assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.patientId)
            }
        }
    }
}
