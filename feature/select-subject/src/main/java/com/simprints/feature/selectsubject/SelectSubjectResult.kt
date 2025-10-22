package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.step.StepResult

@Keep
data class SelectSubjectResult(
    val isSubjectIdSaved: Boolean,
    val savedCredential: ExternalCredential?,
) : StepResult
