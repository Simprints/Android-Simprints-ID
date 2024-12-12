package com.simprints.infra.network.exceptions

import androidx.annotation.Keep

@Keep
internal data class ApiError(
    val error: String?,
)
