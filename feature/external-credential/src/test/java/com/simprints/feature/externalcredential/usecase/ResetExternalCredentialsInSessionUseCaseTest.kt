package com.simprints.feature.externalcredential.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var scannedCredential: ScannedCredential

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: ResetExternalCredentialsInSessionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProject() } returns project

        useCase = ResetExternalCredentialsInSessionUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
            configManager = configManager,
            eventRepository = eventRepository,
            sessionCoroutineScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
        every { scannedCredential.credential } returns CREDENTIAL
        every { scannedCredential.credentialType } returns CREDENTIAL_TYE
    }

    @Test
    fun `invokes enrolment repository with correct update action`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase(scannedCredential, SUBJECT_ID)

        val actionsSlot = slot<List<SubjectAction>>()
        coVerify { enrolmentRecordRepository.performActions(capture(actionsSlot), project) }

        val actions = actionsSlot.captured
        assertThat(actions).hasSize(1)
        val updateAction = actions.first() as SubjectAction.Update
        assertThat(updateAction.subjectId).isEqualTo(SUBJECT_ID)
        assertThat(updateAction.externalCredentialsToAdd).hasSize(1)
        assertThat(updateAction.samplesToAdd).isEmpty()
        assertThat(updateAction.referenceIdsToRemove).isEmpty()
    }

    @Test
    fun `adds correct external credential to subject`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf()

        useCase(scannedCredential, SUBJECT_ID)

        val actionsSlot = slot<List<SubjectAction>>()
        coVerify { enrolmentRecordRepository.performActions(capture(actionsSlot), project) }

        val updateAction = actionsSlot.captured.first() as SubjectAction.Update
        val addedCredential = updateAction.externalCredentialsToAdd.first()
        assertThat(addedCredential.value).isEqualTo(CREDENTIAL)
        assertThat(addedCredential.type).isEqualTo(CREDENTIAL_TYE)
    }

    @Test
    fun `removes correct external credential to subject`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            enrolmentUpdateEvent("subject-1", listOf("credentia-1")),
        )

        useCase(
            scannedCredential = scannedCredential,
            subjectId = SUBJECT_ID,
        )

        val actionsSlot = slot<List<SubjectAction>>()
        coVerify { enrolmentRecordRepository.performActions(capture(actionsSlot), project) }

        // Remove actions come first
        val removeAction = actionsSlot.captured.first() as SubjectAction.Update
        assertThat(removeAction.subjectId).isEqualTo("subject-1")
        assertThat(removeAction.externalCredentialsToAdd).isEmpty()
        assertThat(removeAction.externalCredentialIdsToRemove).containsExactly("credentia-1")
        // Additions come after
        val addAction = actionsSlot.captured.last() as SubjectAction.Update
        assertThat(addAction.externalCredentialsToAdd).isNotEmpty()
        assertThat(addAction.externalCredentialIdsToRemove).isEmpty()
    }

    @Test
    fun `remove existing update events in the session`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            otherEvent(),
            enrolmentUpdateEvent("subject-1", listOf("credentia-1")),
            otherEvent(),
        )

        useCase(
            scannedCredential = scannedCredential,
            subjectId = SUBJECT_ID,
        )

        coEvery { eventRepository.deleteEvents(match { it.size == 1 }) }
    }

    @Test
    fun `does not add credentials to any subject if no subjectID`() = runTest {
        useCase(
            scannedCredential = scannedCredential,
            subjectId = "none_selected",
        )

        val actionsSlot = slot<List<SubjectAction>>()
        coVerify { enrolmentRecordRepository.performActions(capture(actionsSlot), project) }

        assertThat(actionsSlot.captured).isEmpty()
    }

    @Test
    fun `retrieves project using correct project id`() = runTest {
        useCase(scannedCredential, SUBJECT_ID)
        coVerify { configManager.getProject() }
    }

    private fun enrolmentUpdateEvent(
        subjectId: String,
        credentialIds: List<String>,
    ) = EnrolmentUpdateEvent(
        createdAt = Timestamp(0L),
        subjectId = subjectId,
        externalCredentialIdsToAdd = credentialIds,
    )

    private fun otherEvent() = ExternalCredentialSelectionEvent(
        Timestamp(0L),
        Timestamp(1L),
        CREDENTIAL_TYE,
    )

    companion object {
        private const val SUBJECT_ID = "bbaa8ff3-34f7-41d3-a6c9-ff3b952d832e"
        private val CREDENTIAL = "credential".asTokenizableEncrypted()
        private val CREDENTIAL_TYE = ExternalCredentialType.NHISCard
    }
}
