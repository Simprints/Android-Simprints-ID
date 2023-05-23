package com.simprints.feature.login

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LoginParams(
    val projectId: String,
    val userId: String,
): Parcelable
