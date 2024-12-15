package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SelectSubjectResult(
    val success: Boolean,
) : Serializable
