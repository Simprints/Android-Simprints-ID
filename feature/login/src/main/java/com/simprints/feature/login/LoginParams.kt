package com.simprints.feature.login

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString

@Keep
data class LoginParams(
    val projectId: String,
    val userId: TokenizableString,
) : StepParams
