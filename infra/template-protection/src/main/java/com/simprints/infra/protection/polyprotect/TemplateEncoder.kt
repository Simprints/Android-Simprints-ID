package com.simprints.infra.protection.polyprotect

import com.simprints.infra.protection.auxiliary.AuxData
import javax.inject.Inject
import kotlin.math.pow

class TemplateEncoder @Inject constructor() {

    /**
     * Convenience method for formats used by FaceNet
     */
    fun encodeTemplate(
        template: FloatArray,
        auxData: AuxData,
        overlap: Int = OVERLAP,
    ): FloatArray = encodeTemplate(
        template.map { it.toDouble() },
        auxData,
        overlap
    ).map { it.toFloat() }.toFloatArray()

    fun encodeTemplate(
        template: List<Double>,
        auxData: AuxData,
        overlap: Int = OVERLAP,
    ): List<Double> {
        val (e, c) = auxData // For convenience
        assert(e.size == c.size) { "Auxiliary data sizes must be equal" }

        val stepSize = auxData.e.size - overlap
        val eIndices = e.indices

        val protectedTemplate = mutableListOf<Double>()
        for (templateIndex in 0..(template.lastIndex - overlap) step stepSize) {
            val s = eIndices
                .map { i ->
                    // If the target element is out of bounds, consider it 0 since 0^n==0
                    // This would be the same as padding the provided array up to certain size
                    if (templateIndex + i > template.lastIndex) {
                        0.0
                    } else {
                        template[templateIndex + i].pow(e[i]).times(c[i])
                    }
                }
                .sum()
            protectedTemplate.add(s)
        }
        return protectedTemplate
    }


    companion object {

        // TODO move this to configuration
        private const val OVERLAP = 2
    }
}
