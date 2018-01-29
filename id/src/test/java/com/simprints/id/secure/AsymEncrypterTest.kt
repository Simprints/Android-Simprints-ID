package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class AsymmetricEncrypterTest {

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val encryptedString = AsymmetricEncrypter().encrypt(string)
        assertNotNull(encryptedString)
        assert(encryptedString.isNotEmpty())
    }

    @Test
    fun testEncryptingTheSameStringGivesTheSameResult() {
        val string1 = "this is some string"
        val string2 = "this is some string"
        val encryptedString1 = AsymmetricEncrypter().encrypt(string1)
        val encryptedString2 = AsymmetricEncrypter().encrypt(string2)
        assertEquals(encryptedString1, encryptedString2)
    }

    @Test
    fun testEncryptingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val encryptedString1 = AsymmetricEncrypter().encrypt(string1)
        val encryptedString2 = AsymmetricEncrypter().encrypt(string2)
        assertNotEquals(encryptedString1, encryptedString2)
    }
}
