package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import com.simprints.id.secure.domain.PublicKeyString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class AsymmetricEncrypterTest {

    private val publicKeyString = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMu9L/Apu2nWnBhcBAK" +
        "w+q23vHQ1KSupMgoIO+XZD5BTo3vNkXt2Jqs2xVIKJmRE1yM7Sz0BlOXDxyVasTHXuPaL9OJ0+BRXx3lXrK/" +
        "Y62LphM/aeHA3m4JacP8S3C5m4ZZieg2h61tzcB1UZFiinR4IpRDhpw85y109Tj4Ar4dwIDAQAB")

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val encryptedString = AsymmetricEncrypter(publicKeyString).encrypt(string)
        assertNotNull(encryptedString)
        assert(encryptedString.isNotEmpty())
    }

    @Test
    fun testEncryptingTheSameStringGivesTheSameResult() {
        val string1 = "this is some string"
        val string2 = "this is some string"
        val encryptedString1 = AsymmetricEncrypter(publicKeyString).encrypt(string1)
        val encryptedString2 = AsymmetricEncrypter(publicKeyString).encrypt(string2)
        assertEquals(encryptedString1, encryptedString2)
    }

    @Test
    fun testEncryptingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val encryptedString1 = AsymmetricEncrypter(publicKeyString).encrypt(string1)
        val encryptedString2 = AsymmetricEncrypter(publicKeyString).encrypt(string2)
        assertNotEquals(encryptedString1, encryptedString2)
    }
}
