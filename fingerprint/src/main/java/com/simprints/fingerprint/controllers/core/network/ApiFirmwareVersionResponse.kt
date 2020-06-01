package com.simprints.fingerprint.controllers.core.network

import androidx.annotation.Keep

@Keep
data class ApiFirmwareVersionResponse(
    val chipType: String,
    val version: String,
    val versionURL: String
)
