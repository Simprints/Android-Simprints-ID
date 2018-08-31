package com.simprints.id.shared

import com.google.common.truth.Correspondence

val INSTANCEOF_CORRESPONDENCE = object : Correspondence<Any, Class<*>>() {

    override fun compare(actual: Any?, expected: Class<*>?): Boolean {
        return expected?.isInstance(actual) ?: false
    }

    override fun toString(): String {
        return "is instanceof"
    }
}
