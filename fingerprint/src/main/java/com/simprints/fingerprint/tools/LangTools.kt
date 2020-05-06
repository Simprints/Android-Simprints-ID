package com.simprints.fingerprint.tools

inline fun <K, V, R : Any> Map<K, V>.mapNotNullValues(transform: (V) -> R?): Map<K, R> =
    mapNotNull { (k, v) -> transform(v)?.let { k to it } }.toMap()
