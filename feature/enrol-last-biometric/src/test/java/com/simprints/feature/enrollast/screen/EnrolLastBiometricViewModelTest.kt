package com.simprints.feature.enrollast.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.usecase.BuildSubjectUseCase
import com.simprints.feature.enrollast.screen.usecase.HasDuplicateEnrolmentsUseCase
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EnrolLastBiometricViewModelTest {
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
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

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

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            every { id } returns SESSION_ID
        }
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<PersonCreationEvent> { every { id } returns SESSION_ID },
        )

        viewModel = EnrolLastBiometricViewModel(
            timeHelper,
            configManager,
            eventRepository,
            enrolmentRecordRepository,
            hasDuplicateEnrolments,
            buildSubject,
        )
    }

    @Test
    fun `only calls enrol once`() = runTest {
        viewModel.onViewCreated(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId"),
                ),
            ),
        )
        viewModel.onViewCreated(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId"),
                ),
            ),
        )

        coVerify(exactly = 1) { configManager.getProjectConfiguration() }
    }

    @Test
    fun `returns success when has previous enrolment`() = runTest {
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId"),
                ),
            ),
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result).isEqualTo(EnrolLastState.Success("previousSubjectId"))
    }

    @Test
    fun `does not log event when has previous enrolment`() = runTest {
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId"),
                ),
            ),
        )

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
        coVerify(exactly = 0) { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `returns failure when has previous enrolment without subject`() = runTest {
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                ),
            ),
        )

        val result =
            viewModel.finish
                .test()
                .value()
                .getContentIfNotHandled() as EnrolLastState.Failed
        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.GENERAL_ERROR)
    }

    @Test
    fun `returns failure when has duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns true

        viewModel.enrolBiometric(createParams(listOf()))

        val result =
            viewModel.finish
                .test()
                .value()
                .getContentIfNotHandled() as EnrolLastState.Failed

        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS)
    }

    @Test
    fun `returns success when no duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery { buildSubject.invoke(any()) } returns subject

        viewModel.enrolBiometric(createParams(listOf()))

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Success::class.java)
    }

    @Test
    fun `saves event and record when no duplicate enrolments`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery { buildSubject.invoke(any()) } returns subject

        viewModel.enrolBiometric(createParams(listOf()))

        coVerify { eventRepository.addOrUpdateEvent(any()) }
        coVerify { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `returns failure record saving fails`() = runTest {
        every { hasDuplicateEnrolments.invoke(any(), any()) } returns false
        coEvery { buildSubject.invoke(any()) } returns subject
        coEvery { enrolmentRecordRepository.performActions(any(), any()) } throws Exception()

        viewModel.enrolBiometric(createParams(listOf()))

        val result =
            viewModel.finish
                .test()
                .value()
                .getContentIfNotHandled() as EnrolLastState.Failed
        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.GENERAL_ERROR)
    }

    @Test
    fun `Uses all BiometricReferenceCreationEvent for Enrolment event`() = runTest {
        val biometricReferenceCreationEvent1 = mockk<BiometricReferenceCreationEvent> {
            every { id } returns "biometricReferenceCreationEventId1"
            every { payload } returns mockk<BiometricReferenceCreationPayload> {
                every { createdAt } returns Timestamp(1)
                every { id } returns "referenceId1"
            }
        }
        val biometricReferenceCreationEvent2 = mockk<BiometricReferenceCreationEvent> {
            every { id } returns "biometricReferenceCreationEventId2"
            every { payload } returns mockk<BiometricReferenceCreationPayload> {
                every { createdAt } returns Timestamp(2)
                every { id } returns "referenceId2"
            }
        }

        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            biometricReferenceCreationEvent2,
            biometricReferenceCreationEvent1,
        )

        viewModel.enrolBiometric(createParams(listOf()))

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(EnrolmentEventV4::class.java)
                    assertThat((it.payload as EnrolmentEventV4.EnrolmentPayload).biometricReferenceIds)
                        .containsExactly("referenceId1", "referenceId2")
                },
            )
        }
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
