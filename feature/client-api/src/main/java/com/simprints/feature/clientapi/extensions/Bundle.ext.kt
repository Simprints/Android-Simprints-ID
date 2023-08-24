package com.simprints.feature.clientapi.extensions

import android.os.Bundle

internal fun Bundle.toMap(): Map<String, Any> {
    val map = HashMap<String, Any>()
    keySet().forEach { map[it] = get(it) ?: "" }

    return map
}

internal fun Map<String, Any>.extractString(key: String) = getOrElse(key) { "" } as String
