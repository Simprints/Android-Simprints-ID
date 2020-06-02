package com.simprints.fingerprint.tools

inline fun <A, B, R> doIfNotNull(a: A?, b: B?, block: (A, B) -> R): R? =
    if (a == null || b == null) {
        null
    } else {
        block(a, b)
    }
