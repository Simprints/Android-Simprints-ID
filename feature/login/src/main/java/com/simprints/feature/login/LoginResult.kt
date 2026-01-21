package com.simprints.feature.login

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("LoginResult")
data class LoginResult(
    val isSuccess: Boolean,
    val error: LoginError? = null,
) : StepResult

@Keep
@Serializable
enum class LoginError {
    LoginNotCompleted,

    IntegrityServiceError,
    MissingPlayServices,
    OutdatedPlayServices,
    MissingOrOutdatedPlayServices,
    Unknown,
}
