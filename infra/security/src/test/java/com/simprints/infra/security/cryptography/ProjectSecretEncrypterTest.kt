package com.simprints.infra.security.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectSecretEncrypterTest {

    private val publicKeyString =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB"

    @Test
    fun `encrypt should encrypt the string`() {
        val string = "this is some string"
        val encryptedString = ProjectSecretEncrypter(publicKeyString).encrypt(string)
        assertThat(encryptedString).isNotEmpty()
    }

    @Test
    fun `encrypt should return different results for different strings`() {
        val string1 = "this is some string"
        val string2 = "this is another string"
        val encryptedString1 = ProjectSecretEncrypter(publicKeyString).encrypt(string1)
        val encryptedString2 = ProjectSecretEncrypter(publicKeyString).encrypt(string2)
        assertThat(encryptedString1).isNotEqualTo(encryptedString2)
    }
}
