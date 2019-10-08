package com.simprints.fingerprintscanner.v2.tools.primitives

fun stripWhiteSpaceAndMakeLowercase(string: String) = string
    .replace(" ", "")
    .replace("\n", "")
    .replace("\r", "")
    .toLowerCase()
