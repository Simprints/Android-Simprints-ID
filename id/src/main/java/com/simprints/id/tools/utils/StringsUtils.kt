package com.simprints.id.tools.utils

import java.util.*

class StringsUtils {
    companion object {
        fun randomUUID(): String {
            return UUID.randomUUID().toString()
        }
    }
}
