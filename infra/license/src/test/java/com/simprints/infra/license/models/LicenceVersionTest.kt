package com.simprints.infra.license.models

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class LicenceVersionTest {

    @Test
    fun `correctly returns if is unlimited`() {
        assertThat(LicenseVersion("").isLimited).isFalse()
        assertThat(LicenseVersion.UNLIMITED.isLimited).isFalse()

        assertThat(LicenseVersion("1.1").isLimited).isTrue()
    }
}
