package com.simprints.id.testSnippets

import com.simprints.id.tools.RandomGenerator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

fun setupRandomGeneratorToGenerateKey(realmKey: ByteArray?, randomGeneratorMock: RandomGenerator) {
    Mockito.doReturn(realmKey).`when`(randomGeneratorMock).generateByteArray(ArgumentMatchers.anyInt())
}
