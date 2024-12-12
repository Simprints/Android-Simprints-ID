package com.simprints.fingerprint.infra.scanner.testtools

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.stripWhiteSpaceToLowercase

fun assertHexStringsEqual(
    expected: String,
    actual: String,
) {
    assertThat(expected.stripWhiteSpaceToLowercase())
        .isEqualTo(actual.stripWhiteSpaceToLowercase())
}

fun assertHexStringsEqual(
    expected: List<String>,
    actual: List<String>,
) {
    assertThat(actual.map { it.stripWhiteSpaceToLowercase() })
        .containsExactlyElementsIn(expected.map { it.stripWhiteSpaceToLowercase() })
        .inOrder()
}
