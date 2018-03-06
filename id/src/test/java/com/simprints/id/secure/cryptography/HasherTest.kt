package com.simprints.id.secure.cryptography

import com.simprints.id.BuildConfig
import com.simprints.id.tools.roboletric.TestApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class HasherTest {

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val hashedString = Hasher.hash(string)
        Assert.assertNotNull(hashedString)
        assert(hashedString.isEmpty())
    }

    @Test
    fun testHashingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val hashedString1 = Hasher.hash(string1)
        val hashedString2 = Hasher.hash(string2)
        Assert.assertNotEquals(hashedString1, hashedString2)
    }

    @Test
    fun testHashingSameStringsGivesSameResults() {
        val string1 = "this is some string"
        val string2 = "this is some string"
        val hashedString1 = Hasher.hash(string1)
        val hashedString2 = Hasher.hash(string2)
        Assert.assertEquals(hashedString1, hashedString2)
    }
}
