package com.simprints.id.testSnippets

import com.simprints.id.Application
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.tools.RandomGenerator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import shared.anyNotNull

fun mockSecureDataManagerToGenerateKey(app: Application, realmKey: ByteArray?) {
    val randomGenerator = Mockito.mock(RandomGenerator::class.java)
    Mockito.doReturn(realmKey).`when`(randomGenerator).generateByteArray(ArgumentMatchers.anyInt(), anyNotNull())
    app.secureDataManager = SecureDataManagerImpl(app.keyStoreManager, app.preferencesManager, randomGenerator)
}
