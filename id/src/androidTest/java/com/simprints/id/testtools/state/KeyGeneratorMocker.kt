package com.simprints.id.testtools.state

import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.syntax.whenever
import org.mockito.ArgumentMatchers.anyInt

fun setupRandomGeneratorToGenerateKey(realmKey: ByteArray, randomGeneratorMock: RandomGenerator) {
    whenever { randomGeneratorMock.generateByteArray(anyInt()) } thenReturn realmKey
}
