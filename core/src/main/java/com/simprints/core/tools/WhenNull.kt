package com.simprints.core.tools

inline fun <T> T?.whenNull(block: T?.() -> Unit): T? = apply {
    if (this == null) block()
}

inline fun <T> T?.whenNonNull(block: T.() -> Unit): T? = apply {
    this?.block()
}
