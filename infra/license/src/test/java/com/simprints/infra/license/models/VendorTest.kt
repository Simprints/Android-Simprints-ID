package com.simprints.infra.license.models

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class VendorTest {
    @Test
    fun `correctly maps keys`() {
        assertThat(Vendor.fromKey("RANK_ONE_FACE")).isEqualTo(Vendor.RankOne)
        assertThat(Vendor.fromKey("NEC_FINGERPRINT")).isEqualTo(Vendor.Nec)
    }

    @Test
    fun `handles incorrect vendors`() {
        assertThrows<IllegalStateException> { Vendor.fromKey("INVALID") }
    }
}
