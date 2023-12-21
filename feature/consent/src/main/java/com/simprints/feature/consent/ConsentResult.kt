package com.simprints.feature.consent

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ConsentResult(
    val accepted: Boolean,
) : Serializable
