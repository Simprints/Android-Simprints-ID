package com.simprints.feature.enrollast

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class EnrolLastBiometricResult(
    val newSubjectId: String?,
) : Serializable
