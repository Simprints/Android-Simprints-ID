package com.simprints.core.tools.extentions

import java.util.UUID

fun String.isValidGuid() = try {
    UUID.fromString(this)
    true
} catch (_: IllegalArgumentException) {
    false
}
