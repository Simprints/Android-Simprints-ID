package com.simprints.id.secure.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ProjectSecretEncrypterTest {

    private val publicKeyString = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

    @Test
    fun testCanEncryptString() {
        val string = "this is some string"
        val encryptedString = ProjectSecretEncrypter(publicKeyString).encrypt(string)
        assertTrue(encryptedString.isNotEmpty())
    }

    @Test
    fun testEncryptingDifferentStringsGivesDifferentResults() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val encryptedString1 = ProjectSecretEncrypter(publicKeyString).encrypt(string1)
        val encryptedString2 = ProjectSecretEncrypter(publicKeyString).encrypt(string2)
        assertNotEquals(encryptedString1, encryptedString2)
    }
}
