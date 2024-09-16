package com.simprints.infra.enrolment.records.store.domain.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SubjectQuery(
    val projectId: String? = null,
    val subjectId: String? = null,
    val subjectIds: List<String>? = null,
    val attendantId: String? = null,
    val fingerprintSampleFormat: String? = null,
    val faceSampleFormat: String? = null,
    val hasUntokenizedFields: Boolean? = null,
    val moduleId: String? = null,
    val sort: Boolean = false,
    val afterSubjectId: String? = null,
    val metadata: String? = null,
) : Serializable
