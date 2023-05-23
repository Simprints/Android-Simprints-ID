package com.simprints.feature.login

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LoginResult(
    val isSuccess: Boolean,
    // TODO add flag for different possible errors during login
) : Parcelable
