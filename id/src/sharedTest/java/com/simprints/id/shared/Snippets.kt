package com.simprints.id.shared

import com.simprints.id.data.secure.keystore.KeystoreManager
import org.mockito.Mockito
import org.mockito.stubbing.Answer

fun setupFakeKeyStore(): KeystoreManager = Mockito.mock(KeystoreManager::class.java).also { keystoreManager ->
    val encryptAnswer = Answer<String> {
        "enc_" + it.arguments[0] as String
    }
    Mockito.doAnswer(encryptAnswer).`when`(keystoreManager).encryptString(anyNotNull())

    val decryptAnswer = Answer<String> {
        (it.arguments[0] as String).replace("enc_", "")
    }
    Mockito.doAnswer(decryptAnswer).`when`(keystoreManager).decryptString(anyNotNull())
}
