package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SubjectQuery(val projectId: String? = null,
                        val subjectId: String? = null,
                        val attendantId: String? = null,
                        val moduleId: String? = null) : Serializable
