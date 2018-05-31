package com.simprints.id.shared

import com.simprints.id.data.secure.keystore.KeystoreManager
import org.mockito.Mockito
import org.mockito.stubbing.Answer

fun setupFakeKeyStore(): KeystoreManager = Mockito.mock(KeystoreManager::class.java).also {
    val encryptAnswer = Answer<String> {
        "enc_" + it.arguments[0] as String
    }
    Mockito.doAnswer(encryptAnswer).`when`(it).encryptString(anyNotNull())

    val decryptAnswer = Answer<String> {
        (it.arguments[0] as String).replace("enc_", "")
    }
    Mockito.doAnswer(decryptAnswer).`when`(it).decryptString(anyNotNull())
}
