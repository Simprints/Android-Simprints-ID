package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import java.io.Serializable

@Keep
data class SubjectQuery(
    val projectId: String? = null,
    val subjectId: String? = null,
    val subjectIds: List<String>? = null,
    val attendantId: TokenizableString? = null,
    val fingerprintSampleFormat: String? = null,
    val faceSampleFormat: String? = null,
    val hasUntokenizedFields: Boolean? = null,
    val moduleId: TokenizableString? = null,
    val sort: Boolean = false,
    val afterSubjectId: String? = null,
    val metadata: String? = null,
) : Serializable
