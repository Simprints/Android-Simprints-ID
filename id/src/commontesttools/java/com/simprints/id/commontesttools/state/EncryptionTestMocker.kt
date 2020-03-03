package com.simprints.id.commontesttools.state

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.mockk.every
import org.mockito.ArgumentMatchers.anyInt
import kotlin.random.Random

fun setupFakeEncryptedSharedPreferences(ctx: Context): SharedPreferences {
    return ctx.getSharedPreferences("test", 0)
}

fun setupFakeKeyStore(keystoreManager: KeystoreManager) {
    every { keystoreManager.decryptString(anyNotNull()) } answers {
        (this.args[0] as String).replace("enc_", "")
    }
}

fun setupRandomGeneratorToGenerateKey(randomGeneratorMock: RandomGenerator) {
    whenever(randomGeneratorMock) { generateByteArray(anyInt()) } thenAnswer {
        Random(0).nextBytes(ByteArray(it.arguments[0] as Int))
    }
}
