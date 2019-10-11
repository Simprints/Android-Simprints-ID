package com.simprints.fingerprintscanner.v2.tools.primitives

fun String.stripWhiteSpaceToLowercase() = this
    .replace(" ", "")
    .replace("\n", "")
    .replace("\r", "")
    .toLowerCase()
