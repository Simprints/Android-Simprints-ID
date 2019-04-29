package com.simprints.id.commontesttools.state

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.stubbing.Answer
import kotlin.random.Random

fun setupFakeKeyStore(keystoreManager: KeystoreManager) {
    val encryptAnswer = Answer<String> {
        "enc_" + it.arguments[0] as String
    }
    whenever { keystoreManager.encryptString(anyNotNull()) } thenAnswer encryptAnswer

    val decryptAnswer = Answer<String> {
        (it.arguments[0] as String).replace("enc_", "")
    }
    whenever { keystoreManager.decryptString(anyNotNull()) } thenAnswer decryptAnswer
}

fun setupRandomGeneratorToGenerateKey(randomGeneratorMock: RandomGenerator) {
    whenever(randomGeneratorMock) { generateByteArray(anyInt()) } thenAnswer {
        Random(0).nextBytes(ByteArray(it.arguments[0] as Int))
    }
}
