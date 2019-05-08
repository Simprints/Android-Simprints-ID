package com.simprints.clientapi.extensions

import android.os.Bundle

fun Bundle.toMap(): Map<String, Any> {
    val map = HashMap<String, Any>()
    keySet().forEach { map[it] = get(it) ?: "" }

    return map
}
