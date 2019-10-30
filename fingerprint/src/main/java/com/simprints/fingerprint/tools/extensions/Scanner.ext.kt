package com.simprints.fingerprint.tools.extensions

import com.simprints.fingerprintscanner.v1.Scanner

fun Scanner.getUcVersionString(): String =
    if (ucVersion > -1) ucVersion.toString() else "unknown"
