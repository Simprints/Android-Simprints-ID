package com.simprints.feature.login

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LoginResult(
    val isSuccess: Boolean,
    val error: LoginError? = null,
) : Parcelable

@Keep
enum class LoginError {

    LoginNotCompleted,

    MissingPlayServices,
    OutdatedPlayServices,
    ;

}
