package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ListExtTest {
    @Test
    fun `Correctly updates items at index`() {
        val list = listOf(1, 2, 3)

        assertThat(list.updateOnIndex(1) { 7 }).isEqualTo(listOf(1, 7, 3))
    }

    @Test
    fun `Ignores out of bounds indexes`() {
        val list = listOf(1, 2, 3)

        assertThat(list.updateOnIndex(5) { 7 }).isEqualTo(listOf(1, 2, 3))
        assertThat(list.updateOnIndex(-2) { 7 }).isEqualTo(listOf(1, 2, 3))
    }
}
