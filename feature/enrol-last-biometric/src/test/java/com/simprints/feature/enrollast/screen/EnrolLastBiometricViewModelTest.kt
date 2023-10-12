package com.simprints.feature.enrollast.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.usecase.BuildSubjectUseCase
import com.simprints.feature.enrollast.screen.usecase.HasDuplicateEnrolmentsUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnrolLastBiometricViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var projectConfig: ProjectConfiguration

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var hasDuplicateEnrolments: HasDuplicateEnrolmentsUseCase

    @MockK
    lateinit var buildSubject: BuildSubjectUseCase

    @MockK
    lateinit var subject: Subject

    private lateinit var viewModel: EnrolLastBiometricViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration() } returns projectConfig
        every { projectConfig.general.modalities } returns emptyList()

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns mockk {
            every { id } returns SESSION_ID
        }
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<PersonCreationEvent> { every { id } returns SESSION_ID }
        )

        viewModel = EnrolLastBiometricViewModel(
            timeHelper,
            configManager,
            eventRepository,
            enrolmentRecordManager,
            hasDuplicateEnrolments,
            buildSubject
        )
    }

    @Test
    fun `returns success when has previous enrolment`() = runTest {
        viewModel.enrolBiometric(createParams(listOf(
            EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId")
        )))

        val result = viewModel.finish.test().value().getContentIfNotHandled()
        assertThat(result).isEqualTo(EnrolLastState.Success("previousSubjectId"))
    }

    @Test
    fun `does not log event when has previous enrolment`() = runTest {
        viewModel.enrolBiometric(createParams(listOf(
            EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId")
        )))

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
        coVerify(exactly = 0) { enrolmentRecordManager.performActions(any()) }
    }

    @Test
    fun `returns failure when has previous enrolment without subject`() = runTest {
        viewModel.enrolBiometric(createParams(listOf(
            EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null)
        )))

        val result = viewModel.finish.test().value().getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Failed::class.java)
    }

    @Test
    fun `returns failure when has duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns true

        viewModel.enrolBiometric(createParams(listOf()))

        val result = viewModel.finish.test().value().getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Failed::class.java)
    }

    @Test
    fun `returns success when no duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery {  buildSubject.invoke(any()) } returns subject

        viewModel.enrolBiometric(createParams(listOf()))

        val result = viewModel.finish.test().value().getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Success::class.java)
    }

    @Test
    fun `saves event and record when no duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery { buildSubject.invoke(any()) } returns subject

        viewModel.enrolBiometric(createParams(listOf()))

        coVerify { eventRepository.addOrUpdateEvent(any()) }
        coVerify { enrolmentRecordManager.performActions(any()) }
    }

    @Test
    fun `returns failure record saving fails`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery { buildSubject.invoke(any()) } returns subject
        coEvery { enrolmentRecordManager.performActions(any()) } throws Exception()

        viewModel.enrolBiometric(createParams(listOf()))

        val result = viewModel.finish.test().value().getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Failed::class.java)
    }

    private fun createParams(steps: List<EnrolLastBiometricStepResult>) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val MODULE_ID = "moduleId".asTokenizableRaw()
        private const val SESSION_ID = "sessionId"
    }
}
