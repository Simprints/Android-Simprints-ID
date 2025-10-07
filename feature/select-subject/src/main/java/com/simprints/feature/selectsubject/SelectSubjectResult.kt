package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString

@Keep
data class SelectSubjectResult(
    val isSubjectIdSaved: Boolean,
    val savedCredential: TokenizableString.Tokenized?,
) : StepResult
