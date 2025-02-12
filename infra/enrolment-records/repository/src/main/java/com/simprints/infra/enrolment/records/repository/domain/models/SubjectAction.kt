package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample

@Keep
sealed class SubjectAction {
    data class Creation(
        val subject: Subject,
    ) : SubjectAction()

    data class Update(
        val subjectId: String,
        val faceSamplesToAdd: List<FaceSample>,
        val fingerprintSamplesToAdd: List<FingerprintSample>,
        val referenceIdsToRemove: List<String>,
    ) : SubjectAction()

    data class Deletion(
        val subjectId: String,
    ) : SubjectAction()
}
