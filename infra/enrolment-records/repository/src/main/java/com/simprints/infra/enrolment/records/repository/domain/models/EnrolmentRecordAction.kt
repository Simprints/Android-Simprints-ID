package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.reference.BiometricReference

@Keep
sealed class EnrolmentRecordAction {
    data class Creation(
        val enrolmentRecord: EnrolmentRecord,
    ) : EnrolmentRecordAction()

    data class Update(
        val subjectId: String,
        val samplesToAdd: List<BiometricReference>,
        val referenceIdsToRemove: List<String>,
        val externalCredentialsToAdd: List<ExternalCredential>,
        val externalCredentialIdsToRemove: List<String>,
    ) : EnrolmentRecordAction()

    data class Deletion(
        val subjectId: String,
    ) : EnrolmentRecordAction()
}
