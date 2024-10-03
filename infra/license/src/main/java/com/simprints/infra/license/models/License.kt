package com.simprints.infra.license.models

import androidx.annotation.Keep

@Keep
data class License(
    val expiration: String?,
    val data: String,
    val version: LicenseVersion,
)
