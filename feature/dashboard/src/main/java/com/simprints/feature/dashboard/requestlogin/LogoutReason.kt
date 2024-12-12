package com.simprints.feature.dashboard.requestlogin

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LogoutReason(
    val title: String,
    val body: String,
) : Parcelable
