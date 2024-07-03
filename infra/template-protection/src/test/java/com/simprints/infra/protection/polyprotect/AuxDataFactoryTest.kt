package com.simprints.infra.protection.polyprotect

import com.google.common.truth.Truth.*
import com.simprints.infra.protection.auxiliary.AuxDataFactory
import org.junit.Test

class AuxDataFactoryTest {

    private val subject = AuxDataFactory()

    @Test
    fun `auxData exponents have correct items`() {
        val aux1 = subject.createAuxData(
            arraySize = 5
        )
        assertThat(aux1.exponents.toSet()).containsExactly(1, 2, 3, 4, 5)
    }

    @Test
    fun `generates different auxData for each call`() {
        val aux1 = subject.createAuxData()
        val aux2 = subject.createAuxData()

        println(aux1)
        println(aux2)

        assertThat(aux1.exponents.size).isEqualTo(aux2.exponents.size)
        assertThat(aux1.exponents).isNotEqualTo(aux2.exponents)

        assertThat(aux1.coefficients.size).isEqualTo(aux2.coefficients.size)
        assertThat(aux1.coefficients).isNotEqualTo(aux2.coefficients)
    }
}
