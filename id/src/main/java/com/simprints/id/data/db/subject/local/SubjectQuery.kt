package com.simprints.id.data.db.subject.local

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SubjectQuery(
    val projectId: String? = null,
    val subjectId: String? = null,
    val subjectIds: List<String>? = null,
    val attendantId: String? = null,
    val moduleId: String? = null,
    val sort: Boolean = false,
    val afterSubjectId: String? = null
) : Serializable
