package com.simprints.feature.enrollast.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.usecase.BuildRecordUseCase
import com.simprints.feature.enrollast.screen.usecase.CheckForDuplicateEnrolmentsUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.usecase.ResetExternalCredentialsInSessionUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    lateinit var project: Project

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var checkForDuplicateEnrolments: CheckForDuplicateEnrolmentsUseCase

    @MockK
    lateinit var buildRecord: BuildRecordUseCase

    @MockK
    lateinit var enrolmentRecord: EnrolmentRecord

    @MockK
    lateinit var scannedCredential: ScannedCredential

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    lateinit var resetEnrolmentUpdateEventsFromSession: ResetExternalCredentialsInSessionUseCase

    private lateinit var viewModel: EnrolLastBiometricViewModel
    private val guidToEnrol = "guidToEnrol"

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
        coJustRun { resetEnrolmentUpdateEventsFromSession.invoke(any()) }

        every { enrolmentRecord.subjectId } returns guidToEnrol

        viewModel = EnrolLastBiometricViewModel(
            timeHelper = timeHelper,
            configManager = configManager,
            eventRepository = eventRepository,
            enrolmentRecordRepository = enrolmentRecordRepository,
            checkForDuplicateEnrolments = checkForDuplicateEnrolments,
            tokenizationProcessor = tokenizationProcessor,
            buildSubject = buildRecord,
            resetEnrolmentUpdateEventsFromSession = resetEnrolmentUpdateEventsFromSession,
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
            isAddingCredential = false,
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result).isEqualTo(EnrolLastState.Success(newGuid = "previousSubjectId", externalCredential = null))
    }

    @Test
    fun `does not log event when has previous enrolment`() = runTest {
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult("previousSubjectId"),
                ),
            ),
            isAddingCredential = false,
        )

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
        coVerify(exactly = 0) { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `returns failure when project is not available`() = runTest {
        coEvery { configManager.getProject() } returns null
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                ),
            ),
            isAddingCredential = false,
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled() as EnrolLastState.Failed
        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.GENERAL_ERROR)
    }

    @Test
    fun `returns failure when has previous enrolment without subject`() = runTest {
        viewModel.enrolBiometric(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                ),
            ),
            isAddingCredential = false,
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
        every { checkForDuplicateEnrolments.invoke(any(), any()) } returns EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS

        viewModel.enrolBiometric(createParams(listOf()), isAddingCredential = false)

        val result =
            viewModel.finish
                .test()
                .value()
                .getContentIfNotHandled() as EnrolLastState.Failed

        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS)
    }

    @Test
    fun `returns success when no duplicate enrolments`() = runTest {
        every { checkForDuplicateEnrolments.invoke(any(), any()) } returns null
        coEvery { buildRecord.invoke(any(), any()) } returns enrolmentRecord

        viewModel.enrolBiometric(createParams(listOf()), isAddingCredential = false)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result).isInstanceOf(EnrolLastState.Success::class.java)
    }

    @Test
    fun `saves event and record when no duplicate enrolments`() = runTest {
        every { checkForDuplicateEnrolments.invoke(any(), any()) } returns null
        coEvery { buildRecord.invoke(any(), any()) } returns enrolmentRecord

        viewModel.enrolBiometric(createParams(listOf()), isAddingCredential = false)

        coVerify { eventRepository.addOrUpdateEvent(any()) }
        coVerify { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `returns failure record saving fails`() = runTest {
        every { checkForDuplicateEnrolments.invoke(any(), any()) } returns null
        coEvery { buildRecord.invoke(any(), any()) } returns enrolmentRecord
        coEvery { enrolmentRecordRepository.performActions(any(), any()) } throws Exception()

        viewModel.enrolBiometric(createParams(listOf()), isAddingCredential = false)

        val result =
            viewModel.finish
                .test()
                .value()
                .getContentIfNotHandled() as EnrolLastState.Failed
        assertThat(result.errorType).isEqualTo(EnrolLastState.ErrorType.GENERAL_ERROR)
    }

    @Test
    fun `Uses all BiometricReferenceCreationEvent for Enrolment event`() = runTest {
        every { checkForDuplicateEnrolments.invoke(any(), any()) } returns null
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

        viewModel.enrolBiometric(createParams(listOf()), isAddingCredential = false)

        coVerify { resetEnrolmentUpdateEventsFromSession.invoke(any(), any()) }
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

    @Test
    fun `shows add credential dialog when scanned credential is linked to another subject`() = runTest {
        val decryptedCredential = "decryptedCredential".asTokenizableRaw()
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(enrolmentRecord)
        coEvery { configManager.getProject() } returns project
        coEvery {
            tokenizationProcessor.decrypt(
                encrypted = scannedCredential.credential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        } returns decryptedCredential

        viewModel.onViewCreated(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(subjectId = "anotherSubjectId"),
                ),
            ),
        )

        val result = viewModel.showAddCredentialDialog
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(result).isNotNull()
        assertThat(result?.scannedCredential).isEqualTo(scannedCredential)
        assertThat(result?.displayedCredential).isEqualTo(decryptedCredential)
        coVerify(exactly = 0) { buildRecord.invoke(any(), any()) }
        coVerify(exactly = 0) { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `add credential dialog is not shown when there is no result`() = runTest {
        val decryptedCredential = "decryptedCredential".asTokenizableRaw()
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(enrolmentRecord)
        coEvery { configManager.getProject() } returns project
        coEvery {
            tokenizationProcessor.decrypt(
                encrypted = scannedCredential.credential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        } returns decryptedCredential

        viewModel.onViewCreated(createParams(steps = listOf()))

        viewModel.showAddCredentialDialog.test().assertNoValue()
    }

    @Test
    fun `add credential dialog is not shown when there are no credentials`() = runTest {
        val decryptedCredential = "decryptedCredential".asTokenizableRaw()
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(enrolmentRecord)
        coEvery { configManager.getProject() } returns project
        coEvery {
            tokenizationProcessor.decrypt(
                encrypted = scannedCredential.credential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        } returns decryptedCredential

        viewModel.onViewCreated(
            createParams(
                steps = listOf(EnrolLastBiometricStepResult.EnrolLastBiometricsResult(subjectId = enrolmentRecord.subjectId)),
                credentials = null,
            ),
        )

        viewModel.showAddCredentialDialog.test().assertNoValue()
    }

    @Test
    fun `add credential dialog is not shown when credential is already linked to same subject`() = runTest {
        val decryptedCredential = "decryptedCredential".asTokenizableRaw()
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(enrolmentRecord)
        coEvery { configManager.getProject() } returns project
        coEvery {
            tokenizationProcessor.decrypt(
                encrypted = scannedCredential.credential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        } returns decryptedCredential

        viewModel.onViewCreated(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(subjectId = enrolmentRecord.subjectId),
                ),
            ),
        )

        viewModel.showAddCredentialDialog.test().assertNoValue()
    }

    private fun createParams(
        steps: List<EnrolLastBiometricStepResult>,
        credentials: ScannedCredential? = scannedCredential,
    ) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
        scannedCredential = credentials,
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val MODULE_ID = "moduleId".asTokenizableRaw()
        private const val SESSION_ID = "sessionId"
    }
}
