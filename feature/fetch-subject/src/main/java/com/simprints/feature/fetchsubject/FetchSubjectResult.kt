package com.simprints.feature.fetchsubject

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class FetchSubjectResult(
    val found: Boolean,
    val wasOnline: Boolean = false,
) : Serializable
