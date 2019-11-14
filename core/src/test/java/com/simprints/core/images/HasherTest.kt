package com.simprints.core.images

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HasherTest {

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val hashedString = com.simprints.id.core.Hasher().hash(string)
        assertThat(hashedString).isNotEmpty()
    }

    @Test
    fun testHashingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val hashedString1 = com.simprints.id.core.Hasher().hash(string1)
        val hashedString2 = com.simprints.id.core.Hasher().hash(string2)
        assertThat(hashedString1).isNotEqualTo(hashedString2)
    }

    @Test
    fun testHashingSameStringsGivesSameResults() {
        val string1 = "this is some string"
        val string2 = "this is some string"
        val hashedString1 = com.simprints.id.core.Hasher().hash(string1)
        val hashedString2 = com.simprints.id.core.Hasher().hash(string2)
        assertThat(hashedString1).isEqualTo(hashedString2)
    }
}
