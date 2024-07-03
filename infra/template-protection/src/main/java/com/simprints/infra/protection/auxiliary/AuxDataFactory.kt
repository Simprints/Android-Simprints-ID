package com.simprints.infra.protection.auxiliary

import javax.inject.Inject
import kotlin.also
import kotlin.collections.shuffle
import kotlin.collections.toIntArray
import kotlin.let
import kotlin.random.Random
import kotlin.takeUnless

internal class AuxDataFactory @Inject constructor() {

    fun createAuxData(
        subjectId: String = "",
        arraySize: Int = AUX_ARRAY_SIZE,
        coefficientBounds: Int = AUX_COEFFICIENT_BOUND,
    ): TemplateAuxData = TemplateAuxData(
        subjectId = subjectId,
        exponents = generateExponents(arraySize),
        coefficients = generateCoefficients(arraySize, coefficientBounds)
    )

    /**
     * Returns integers in 1..arraySize in a random order
     */
    private fun generateExponents(arraySize: Int) = IntArray(arraySize) { it + 1 }.also { it.shuffle() }

    /**
     * Returns an array of random integers in the range of `[-rangeBound, 0) U (0, rangeBound]`
     */
    private fun generateCoefficients(arraySize: Int, rangeBound: Int): IntArray {
        val random = Random
        val numbers = mutableSetOf<Int>()
        while (numbers.size < arraySize) {
            random.nextInt(-rangeBound, rangeBound + 1)
                .takeUnless { it == 0 }
                ?.let { numbers.add(it) }
        }
        return numbers.toIntArray()
    }

    companion object {

        // TODO move this to configuration
        private const val AUX_ARRAY_SIZE = 7
        private const val AUX_COEFFICIENT_BOUND = 100
    }
}
