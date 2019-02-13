package com.simprints.id.testSnippets

import com.simprints.id.tools.RandomGenerator
import com.simprints.testframework.common.syntax.whenever
import org.mockito.ArgumentMatchers.anyInt

fun setupRandomGeneratorToGenerateKey(realmKey: ByteArray, randomGeneratorMock: RandomGenerator) {
    whenever { randomGeneratorMock.generateByteArray(anyInt()) } thenReturn realmKey
}
