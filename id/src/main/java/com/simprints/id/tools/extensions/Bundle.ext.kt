package com.simprints.id.tools.extensions

import android.os.Bundle

fun Bundle.putTruncatedString(key: String, value: String?, maxKeyLength: Int, maxValueLength: Int) {
    putString(key.substring(maxKeyLength), value?.substring(0, maxValueLength))
}

fun Bundle.putTruncatedStringMapping(mapping: Map<String, String?>, maxKeyLength: Int, maxValueLength: Int) {
    for ((key, value) in mapping) {
        putTruncatedString(key, value, maxKeyLength, maxValueLength)
    }
}
