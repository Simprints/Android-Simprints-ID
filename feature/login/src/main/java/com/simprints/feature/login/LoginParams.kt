package com.simprints.feature.login

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LoginParams(
    val projectId: String,
    val userId: TokenizableString,
) : StepParams
