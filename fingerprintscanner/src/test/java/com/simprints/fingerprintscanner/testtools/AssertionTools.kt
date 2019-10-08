package com.simprints.fingerprintscanner.testtools

import com.simprints.fingerprintscanner.v2.tools.primitives.stripWhiteSpaceAndMakeLowercase
import org.junit.Assert

fun assertHexStringsEqual(expected: String, actual: String) {
    Assert.assertEquals(
        stripWhiteSpaceAndMakeLowercase(expected),
        stripWhiteSpaceAndMakeLowercase(actual)
    )
}
