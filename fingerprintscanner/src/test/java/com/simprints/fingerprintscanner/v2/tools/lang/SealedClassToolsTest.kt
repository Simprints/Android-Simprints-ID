package com.simprints.fingerprintscanner.v2.tools.lang

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SealedClassToolsTest {

    @Test
    fun sealedClass_ableToRetrieveObjects() {
        assertThat(TestSealedClass::class.values())
            .containsExactlyElementsIn(listOf(
                TestSealedClass.A,
                TestSealedClass.B,
                TestSealedClass.C
            ))
    }

    sealed class TestSealedClass {
        object A : TestSealedClass()
        object B : TestSealedClass()
        object C : TestSealedClass()
    }
}
