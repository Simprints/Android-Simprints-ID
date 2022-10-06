package com.simprints.infra.enrolment.records.domain.models

import androidx.annotation.Keep

@Keep
sealed class SubjectAction {
    data class Creation(val subject: Subject): SubjectAction()
    data class Deletion(val subjectId: String): SubjectAction()
}
