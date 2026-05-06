package com.simprints.feature.externalcredential.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.externalcredential.ExternalCredentialMapper
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ResetExternalCredentialsInSessionUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var externalCredentialMapper: ExternalCredentialMapper

    @MockK
    lateinit var credentialSearchResult: ExternalCredentialSearchResult.Complete

    @MockK
    lateinit var mappedCredential: ExternalCredential

    @MockK
    lateinit var enrolmentUpdateEvent: EnrolmentUpdateEvent

    @MockK
    lateinit var otherEvent: ExternalCredentialSelectionEvent

    private lateinit var useCase: ResetExternalCredentialsInSessionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getProject() } returns project

        coEvery {
            externalCredentialMapper.mapExternalCredential(
                searchResult = credentialSearchResult,
                subjectId = SUBJECT_ID,
            )
        } returns mappedCredential

        every { enrolmentUpdateEvent.payload.subjectId } returns PREVIOUS_SUBJECT_ID
        every { enrolmentUpdateEvent.payload.externalCredentialIdsToAdd } returns
            listOf(PREVIOUS_CREDENTIAL_ID)

        every { mappedCredential.id } returns SCAN_ID
        every { mappedCredential.subjectId } returns SUBJECT_ID
        every { mappedCredential.type } returns CREDENTIAL_TYPE
        every { mappedCredential.value } returns ENCRYPTED_CREDENTIAL

        useCase = ResetExternalCredentialsInSessionUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
            configRepository = configRepository,
            eventRepository = eventRepository,
            credentialMapper = externalCredentialMapper,
            sessionCoroutineScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `invokes enrolment repository with correct update action`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        val actions = captureActions()
        assertThat(actions).hasSize(1)
        val updateAction = actions.first() as EnrolmentRecordAction.Update
        assertThat(updateAction.subjectId).isEqualTo(SUBJECT_ID)
        assertThat(updateAction.externalCredentialsToAdd)
            .containsExactly(mappedCredential)
        assertThat(updateAction.samplesToAdd).isEmpty()
        assertThat(updateAction.referenceIdsToRemove).isEmpty()
    }

    @Test
    fun `uses mapped external credential from mapper`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        val actions = captureActions()
        val updateAction = actions.first() as EnrolmentRecordAction.Update
        assertThat(updateAction.externalCredentialsToAdd)
            .containsExactly(mappedCredential)
        coVerify(exactly = 1) {
            externalCredentialMapper.mapExternalCredential(
                searchResult = credentialSearchResult,
                subjectId = SUBJECT_ID,
            )
        }
    }

    @Test
    fun `handles missing project`() = runTest {
        clearMocks(configRepository)

        coEvery { configRepository.getProject() } returns null
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        coVerify(exactly = 0) {
            enrolmentRecordRepository.performActions(any(), any())
        }
    }

    @Test
    fun `removes correct external credential from previously linked subject`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            enrolmentUpdateEvent,
        )

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        val actions = captureActions()
        val removeAction = actions.first() as EnrolmentRecordAction.Update
        assertThat(removeAction.subjectId).isEqualTo(PREVIOUS_SUBJECT_ID)
        assertThat(removeAction.externalCredentialsToAdd).isEmpty()
        assertThat(removeAction.externalCredentialIdsToRemove)
            .containsExactly(PREVIOUS_CREDENTIAL_ID)
        val addAction = actions.last() as EnrolmentRecordAction.Update
        assertThat(addAction.externalCredentialsToAdd)
            .containsExactly(mappedCredential)
        assertThat(addAction.externalCredentialIdsToRemove).isEmpty()
    }

    @Test
    fun `remove existing update events in the session`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            otherEvent,
            enrolmentUpdateEvent,
            otherEvent,
        )

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        coVerify {
            eventRepository.deleteEvents(match { it.size == 1 })
        }
    }

    @Test
    fun `does not add credentials when subjectId is not a valid UUID`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = INVALID_SUBJECT_ID,
        )

        val actions = captureActions()
        assertThat(actions).isEmpty()
        coVerify(exactly = 0) {
            externalCredentialMapper.mapExternalCredential(any(), any())
        }
    }

    @Test
    fun `retrieves project using correct project id`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(
            credentialSearchResult = credentialSearchResult,
            subjectId = SUBJECT_ID,
        )

        coVerify { configRepository.getProject() }
    }

    @Test
    fun `invokes with null credentialSearchResult only removes existing links`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            enrolmentUpdateEvent,
        )

        useCase(
            credentialSearchResult = null,
            subjectId = SUBJECT_ID,
        )

        val actions = captureActions()
        assertThat(actions).hasSize(1)
        val removeAction = actions.first() as EnrolmentRecordAction.Update
        assertThat(removeAction.subjectId).isEqualTo(PREVIOUS_SUBJECT_ID)
        assertThat(removeAction.externalCredentialIdsToRemove)
            .containsExactly(PREVIOUS_CREDENTIAL_ID)
        coVerify(exactly = 0) {
            externalCredentialMapper.mapExternalCredential(any(), any())
        }
    }

    private fun captureActions(): List<EnrolmentRecordAction> {
        val actionsSlot = slot<List<EnrolmentRecordAction>>()

        coVerify {
            enrolmentRecordRepository.performActions(capture(actionsSlot), project)
        }

        return actionsSlot.captured
    }

    companion object {
        private const val SUBJECT_ID = "bbaa8ff3-34f7-41d3-a6c9-ff3b952d832e"
        private const val INVALID_SUBJECT_ID = "none_selected"
        private const val PREVIOUS_SUBJECT_ID = "subject-1"
        private const val PREVIOUS_CREDENTIAL_ID = "credential-1"
        private const val SCAN_ID = "scan-id"

        private val ENCRYPTED_CREDENTIAL =
            "encrypted_credential".asTokenizableEncrypted()

        private val CREDENTIAL_TYPE = ExternalCredentialType.NHISCard
    }
}
