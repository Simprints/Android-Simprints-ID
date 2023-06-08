package com.simprints.feature.enrollast

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class EnrolLastBiometricParams(
    val projectId: String,
    val userId: String,
    val moduleId: String,
) : Parcelable
