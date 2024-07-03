package com.simprints.infra.protection.polyprotect

import com.google.common.truth.Truth.*
import com.simprints.infra.protection.auxiliary.TemplateAuxData
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.math.abs

@RunWith(Parameterized::class)
class PolyProtectTemplateEncoderParameterTest(
    private val given: Pair<List<Double>, TemplateAuxData>,
    private val expected: List<Double>
) {

    private val subject = TemplateEncoder()

    @Test
    fun `encode template works correctly`() {
        val (template, aux) = given
        val encoded = subject.encodeTemplate(template, aux)

        println("expected: $expected")
        println("actual:   $encoded")

        assertThat(encoded.size).isEqualTo(expected.size)
        assertThat(
            expected
                .zip(encoded) { e, a -> abs(e - a) < PRECISION }
                .all { it }
        ).isTrue()
    }

    companion object {
        private const val PRECISION = 1e-8f

        @JvmStatic
        @Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // Full template with overlap 2 and no padding
            arrayOf(
                // Given
                listOf(
                    0.1331335545,
                    0.4449820169,
                    -0.2204751117,
                    0.02803098769,
                    0.0488200593,
                    0.1831083583,
                    -0.007320145106,
                    0.3660261928,
                    -0.3799865143,
                    0.2509288685,
                    -0.1363977355,
                    0.4834820974,
                    0.4518768993,
                    0.3874402622,
                    0.002547916374,
                    0.1281368887,
                    -0.2143639432,
                    -0.4538202661,
                    0.0495447296,
                    -0.2834924903,
                    0.2892785979,
                    0.3398633496,
                    -0.09878652073
                ) to TemplateAuxData(
                    subjectId = "",
                    exponents = intArrayOf(1, 2, 3, 4, 5),
                    coefficients = intArrayOf(10, 20, 30, 40, 50)
                ),
                // Expected
                listOf(4.970039955, 0.8406559131, 1.116541916, 8.376161797, 7.509119748, -0.6948732895, 3.362235598)
            ),
            // This template needs to be padded
            arrayOf(
                // Given
                listOf(
                    0.1331335545,
                    0.4449820169,
                    -0.2204751117,
                    0.02803098769,
                    0.0488200593,
                    0.1831083583,
                    -0.007320145106,
                    0.3660261928,
                    -0.3799865143,
                    0.2509288685,
                    -0.1363977355,
                    0.4834820974,
                    0.4518768993,
                    0.3874402622,
                    0.002547916374,
                    0.1281368887,
                    -0.2143639432,
                    -0.4538202661,
                    0.0495447296,
                    -0.2834924903,
                    0.2892785979,
                ) to TemplateAuxData(
                    subjectId = "",
                    exponents = intArrayOf(1, 2, 3, 4, 5),
                    coefficients = intArrayOf(10, 20, 30, 40, 50)
                ),
                // Expected
                listOf(
                    4.970039955, 0.8406559131, 1.116541916, 8.376161797, 7.509119748, -0.6948732895, 2.829030416
                )
            ),
            // Template with some padding and randomised C and E values
            arrayOf(
                // Given
                listOf(
                    0.4952767838,
                    0.04582746275,
                    0.4912871269,
                    -0.05188964656,
                    0.1710497578,
                    0.4828839484,
                    0.4789628756,
                    -0.1757431758,
                    -0.1946878787,
                    -0.1893160354,
                    -0.2281576101,
                    -0.3595243313,
                    0.144228172,
                    -0.4046463718,
                    0.08317913623,
                    -0.4290612621,
                    -0.03695776633,
                    -0.1710147956,
                    -0.2621668255,
                    -0.00713160069,
                    0.2892785979,
                    0.4425149409
                ) to TemplateAuxData(
                    subjectId = "",
                    exponents = intArrayOf(3, 5, 1, 2, 4),
                    coefficients = intArrayOf(35, -83, -27, 79, 12)
                ),
                // Expected
                listOf(
                    -8.789603428, 5.079566739, 11.98009351, 11.48605497, 13.30302774, 7.28262971, 7.02854756
                )
            ),
        )
    }
}
