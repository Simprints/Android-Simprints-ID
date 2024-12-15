package com.simprints.feature.login

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class LoginResult(
    val isSuccess: Boolean,
    val error: LoginError? = null,
) : Serializable

@Keep
enum class LoginError {
    LoginNotCompleted,

    IntegrityServiceError,
    MissingPlayServices,
    OutdatedPlayServices,
    MissingOrOutdatedPlayServices,
    Unknown,
}
