package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFileUrl(
    val url: String,
)
