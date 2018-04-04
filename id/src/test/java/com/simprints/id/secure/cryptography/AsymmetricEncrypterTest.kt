package com.simprints.id.secure.cryptography

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class AsymmetricEncrypterTest {

    private val publicKeyString = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val encryptedString = AsymmetricEncrypter(publicKeyString).encrypt(string)
        assertTrue(encryptedString.isNotEmpty())
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
