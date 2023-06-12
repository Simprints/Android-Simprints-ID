package com.simprints.feature.enrollast

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class EnrolLastBiometricResult(
    val newSubjectId: String?
) : Parcelable
