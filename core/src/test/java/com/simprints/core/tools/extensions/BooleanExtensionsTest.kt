package com.simprints.core.tools.extensions

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.nand
import org.junit.Test

class BooleanExtensionsTest {

    @Test
    fun aFalseNandBFalse_shouldBeTrue() {
        assertThat(false nand false).isTrue()
    }

    @Test
    fun aFalseNandBTrue_shouldBeTrue() {
        assertThat(false nand true).isTrue()
    }

    @Test
    fun aTrueNandBFalse_shouldBeTrue() {
        assertThat(true nand false).isTrue()
    }

    @Test
    fun aTrueNandBTrue_shouldBeFalse() {
        assertThat(true nand true).isFalse()
    }

}
