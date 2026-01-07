package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import javax.inject.Inject

class TokenizeRecordsIfKeysChangedUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) {
    suspend operator fun invoke(
        oldProject: Project?,
        newProject: Project,
    ) {
        if ((oldProject == null || oldProject.tokenizationKeys.isEmpty()) && newProject.tokenizationKeys.isNotEmpty()) {
            enrolmentRecordRepository.tokenizeExistingRecords(newProject)
        }
    }
}
