package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString

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
    val externalCredential: TokenizableString.Tokenized? = null,
) : StepParams
