package com.simprints.fingerprintscanner.v2.tools.primitives

fun String.stripWhiteSpaceToLowercase() = this
    .replace("\\s+".toRegex(), "")
    .toLowerCase()
