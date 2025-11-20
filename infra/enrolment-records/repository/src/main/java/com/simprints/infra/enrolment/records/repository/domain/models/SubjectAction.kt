package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.Sample

@Keep
sealed class SubjectAction {
    data class Creation(
        val subject: Subject,
    ) : SubjectAction()

    data class Update(
        val subjectId: String,
        val samplesToAdd: List<Sample>,
        val referenceIdsToRemove: List<String>,
        val externalCredentialsToAdd: List<ExternalCredential>,
        val externalCredentialIdsToRemove: List<String>,
    ) : SubjectAction()

    data class Deletion(
        val subjectId: String,
    ) : SubjectAction()
}
