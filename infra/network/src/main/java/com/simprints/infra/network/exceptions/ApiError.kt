package com.simprints.infra.network.exceptions

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiError(
    val error: String?,
)
