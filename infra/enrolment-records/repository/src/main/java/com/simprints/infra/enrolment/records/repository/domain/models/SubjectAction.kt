package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep

@Keep
sealed class SubjectAction {
    data class Creation(
        val subject: Subject,
    ) : SubjectAction()

    data class Deletion(
        val subjectId: String,
    ) : SubjectAction()
}
