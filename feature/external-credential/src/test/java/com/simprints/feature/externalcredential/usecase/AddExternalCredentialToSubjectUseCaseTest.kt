package com.simprints.feature.externalcredential.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class AddExternalCredentialToSubjectUseCaseTest {
    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var scannedCredential: ScannedCredential

    private lateinit var useCase: AddExternalCredentialToSubjectUseCase

    private val projectId = "projectId"
    private val subjectId = "subjectId"
    private val encryptedCredential = "credential".asTokenizableEncrypted()
    private val mockCredentialType = ExternalCredentialType.NHISCard

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProject(projectId) } returns project

        useCase = AddExternalCredentialToSubjectUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
            configManager = configManager,
        )
        every { scannedCredential.credential } returns encryptedCredential
        every { scannedCredential.credentialType } returns mockCredentialType
    }

    @Test
    fun `invokes enrolment repository with correct update action`() = runTest {
        val actionsSlot = slot<List<SubjectAction>>()
        useCase(scannedCredential, subjectId, projectId)
        coVerify {
            enrolmentRecordRepository.performActions(
                capture(actionsSlot),
                project,
            )
        }

        val actions = actionsSlot.captured
        assertThat(actions).hasSize(1)
        val updateAction = actions.first() as SubjectAction.Update
        assertThat(updateAction.subjectId).isEqualTo(subjectId)
        assertThat(updateAction.externalCredentialsToAdd).hasSize(1)
        assertThat(updateAction.samplesToAdd).isEmpty()
        assertThat(updateAction.referenceIdsToRemove).isEmpty()
    }

    @Test
    fun `adds correct external credential to subject`() = runTest {
        val actionsSlot = slot<List<SubjectAction>>()

        useCase(scannedCredential, subjectId, projectId)

        coVerify {
            enrolmentRecordRepository.performActions(
                capture(actionsSlot),
                project,
            )
        }

        val updateAction = actionsSlot.captured.first() as SubjectAction.Update
        val addedCredential = updateAction.externalCredentialsToAdd.first()
        assertThat(addedCredential.value).isEqualTo(encryptedCredential)
        assertThat(addedCredential.type).isEqualTo(mockCredentialType)
    }

    @Test
    fun `retrieves project using correct project id`() = runTest {
        useCase(scannedCredential, subjectId, projectId)
        coVerify { configManager.getProject(projectId) }
    }
}
