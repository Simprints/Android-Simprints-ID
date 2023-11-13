package com.simprints.core.tools.utils

import java.util.*

private const val REGEX_GUID =
    "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"

fun randomUUID(): String {
    return UUID.randomUUID().toString()
}

/**
 * For a detailed description of what defines a UUID,
 * @see [java.util.UUID]
 */
fun String.isValidGuid(): Boolean = this.matches(REGEX_GUID.toRegex())
