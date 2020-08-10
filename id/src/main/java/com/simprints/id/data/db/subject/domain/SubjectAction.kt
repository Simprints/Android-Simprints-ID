package com.simprints.id.data.db.subject.domain

import androidx.annotation.Keep

@Keep
sealed class SubjectAction {
    class Creation(val subject: Subject): SubjectAction()
    class Deletion(val subjectId: String): SubjectAction()
}
