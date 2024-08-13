package com.simprints.infra.mlkitwrapper.models.edgeface

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import kotlin.collections.chunked
import kotlin.collections.filterNotNull
import kotlin.collections.flatten
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.toFloatArray
import kotlin.collections.toList

internal class ReshapeOp() : TensorOperator {

    override fun apply(p0: TensorBuffer): TensorBuffer {
        val (height, width, dims) = p0.shape

        val outPixels = p0.floatArray
            .toList()
            .chunked(dims)
            .rotateListOfLists()
            .flatten()
            .toFloatArray()

        val output = TensorBufferFloat.createFixedSize(intArrayOf(dims, height, width), DataType.FLOAT32)
        output.loadArray(outPixels)
        return output
    }

    private fun <T> List<List<T>>.rotateListOfLists(): List<List<T>> {
        // Transpose the list
        val transposed = MutableList(this[0].size) { MutableList<T?>(this.size) { null } }
        for (i in this.indices) {
            for (j in this[0].indices) {
                transposed[j][i] = this[i][j]
            }
        }
        return transposed.map { it.filterNotNull() }
    }
}
