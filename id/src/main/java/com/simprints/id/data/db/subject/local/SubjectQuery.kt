package com.simprints.id.data.db.subject.local

import androidx.annotation.Keep
import java.io.Serializable

@Keep
class SubjectQuery(val projectId: String? = null,
                   val subjectId: String? = null,
                   val attendantId: String? = null,
                   val moduleId: String? = null,
                   val toSync: Boolean? = null) : Serializable
