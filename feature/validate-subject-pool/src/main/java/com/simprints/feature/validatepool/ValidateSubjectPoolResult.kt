package com.simprints.feature.validatepool

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ValidateSubjectPoolResult(
    val isValid: Boolean,
) : Serializable
