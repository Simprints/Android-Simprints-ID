package com.simprints.core.domain.common

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class Modality {
    FINGERPRINT,
    FACE,
}
