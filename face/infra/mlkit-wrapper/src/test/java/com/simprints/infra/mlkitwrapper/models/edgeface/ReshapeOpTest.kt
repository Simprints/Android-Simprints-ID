package com.simprints.infra.mlkitwrapper.model

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.mlkitwrapper.models.edgeface.ReshapeOp
import org.junit.Before
import org.junit.Test
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ReshapeOpTest {

    private lateinit var reshapeOp: ReshapeOp

    @Before
    fun setUp() {
        reshapeOp = ReshapeOp()
    }

    @Test
    fun reshapeOpTest() {

        val buffer = TensorBuffer.createFixedSize(intArrayOf(2, 2, 3), DataType.FLOAT32)
        buffer.loadArray(
            // 4 sets of 3 numbers
            intArrayOf(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9,
                10, 11, 12
            )
        )
        val result = reshapeOp.apply(buffer)

        assertThat(result.dataType).isEqualTo(DataType.FLOAT32)
        assertThat(result.shape).isEqualTo(intArrayOf(3, 2, 2))
        assertThat(result.floatArray).isEqualTo(
            // 3 arrays of 4 elements
            floatArrayOf(
                1f, 4f, 7f, 10f,
                2f, 5f, 8f, 11f,
                3f, 6f, 9f, 12f
            )
        )
    }

}
