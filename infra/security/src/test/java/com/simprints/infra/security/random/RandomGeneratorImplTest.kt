package com.simprints.infra.security.random

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RandomGeneratorImplTest {
    @Test
    fun `it should generate bytes everytime`() {
        val generatedKey1 = RandomGeneratorImpl().generateByteArray(64)
        val generatedKey2 = RandomGeneratorImpl().generateByteArray(64)

        assertThat(generatedKey1).isNotEqualTo(generatedKey2)
    }
}
