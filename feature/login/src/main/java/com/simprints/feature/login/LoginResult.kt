package com.simprints.feature.login

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class LoginResult(
    val isSuccess: Boolean,
    val error: LoginError? = null,
) : StepResult

@Keep
enum class LoginError {
    LoginNotCompleted,

    IntegrityServiceError,
    MissingPlayServices,
    OutdatedPlayServices,
    MissingOrOutdatedPlayServices,
    Unknown,
}
