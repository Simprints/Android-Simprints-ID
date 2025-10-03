package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import javax.inject.Inject

internal class FindSubjectsByCredentialUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val tokenizationProcessor: TokenizationProcessor,
) {

    suspend operator fun invoke(credential: String, project: Project): List<Subject> {
        val credential = tokenizationProcessor.encrypt(
            decrypted = credential.asTokenizableRaw(),
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = project,
        ) as TokenizableString.Tokenized
        return enrolmentRecordRepository.load(SubjectQuery(projectId = project.id, externalCredential = credential))
    }

}
