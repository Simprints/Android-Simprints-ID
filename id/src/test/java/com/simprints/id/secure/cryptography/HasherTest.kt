package com.simprints.id.secure.cryptography

import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class HasherTest {

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val hashedString = Hasher().hash(string)
        Assert.assertTrue(hashedString.isNotEmpty())
    }

    @Test
    fun testHashingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val hashedString1 = Hasher().hash(string1)
        val hashedString2 = Hasher().hash(string2)
        Assert.assertNotEquals(hashedString1, hashedString2)
    }

    @Test
    fun testHashingSameStringsGivesSameResults() {
        val string1 = "this is some string"
        val string2 = "this is some string"
        val hashedString1 = Hasher().hash(string1)
        val hashedString2 = Hasher().hash(string2)
        Assert.assertEquals(hashedString1, hashedString2)
    }
}
