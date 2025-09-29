package com.simprints.feature.externalcredential.usecase

import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import javax.inject.Inject

class AddExternalCredentialToSubjectUseCase @Inject() constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configManager: ConfigManager,
) {
    suspend operator fun invoke(
        scannedCredential: ScannedCredential,
        subjectId: String,
        projectId: String,
    ) {
        val project = configManager.getProject(projectId)
        val updateActions = listOf(
            SubjectAction.Update(
                subjectId = subjectId,
                externalCredentialsToAdd = listOf(scannedCredential.toExternalCredential(subjectId)),
                samplesToAdd = emptyList(),
                referenceIdsToRemove = emptyList(),
            ),
        )
        enrolmentRecordRepository.performActions(updateActions, project)
    }
}
