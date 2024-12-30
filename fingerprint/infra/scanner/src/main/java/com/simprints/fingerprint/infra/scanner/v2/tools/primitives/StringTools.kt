package com.simprints.fingerprint.infra.scanner.v2.tools.primitives

fun String.stripWhiteSpaceToLowercase() = this
    .replace("\\s+".toRegex(), "")
    .lowercase()
