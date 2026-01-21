package com.simprints.infra.serialization

import kotlinx.serialization.json.Json

/**
 * Global Json instance.
 * Defined as a top-level variable to allow "One Dot" usage: SimJson.encodeToString(...)
 *
 * TODO: Should be moved to a Hilt DI module and inject it into classes
 *  https://simprints.atlassian.net/browse/MS-1308
 *
 */
val SimJson: Json by lazy {
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }
}
