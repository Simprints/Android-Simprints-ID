package com.simprints.eventsystem.subject.domain

import androidx.annotation.Keep

@Keep
sealed class SubjectAction {
    data class Creation(val subject: Subject): SubjectAction()
    data class Deletion(val subjectId: String): SubjectAction()
}
