/*
 * Copyright 2023 Shubham Panchal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.simprints.infra.mlkitwrapper.models.edgeface

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.simprints.infra.mlkitwrapper.model.MlKitModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import kotlin.apply
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.toFloatArray

// Utility class for EdgeFace model
class EdgeFaceModel(
    private var context: Context,
) : MlKitModel {

    companion object {
        private const val ASSET_NAME = "edgefacexs.tflite"

        // Input image size for EdgeFace model.
        private const val IMG_SIZE = 112

        // Output embedding size
        private const val EMBEDDING_SIZE = 512
    }

    private var interpreter: Interpreter
    private val imageTensorProcessor = TensorProcessor.Builder()
        .add(NormalizeOp(0.5f, 0.5f))
        .add(ReshapeOp())
        .build()

    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            // GPU is not supported

            // Number of threads for computation
            numThreads = 4
            setUseXNNPACK(true)
            useNNAPI = true
        }
        interpreter = Interpreter(
            FileUtil.loadMappedFile(context, ASSET_NAME),
            interpreterOptions
        )
    }


    // Gets an face embedding using FaceNet.
    override fun getFaceEmbedding(image: Bitmap): FloatArray = runEdgeFace(convertBitmapToBuffer(image))[0]

    // Run the FaceNet model.
    private fun runEdgeFace(inputs: Any): Array<FloatArray> {
        val t1 = System.currentTimeMillis()
        val modelOutput = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(inputs, modelOutput)
        Log.i(
            "Performance",
            "EdgeFace Inference Speed in ms : ${System.currentTimeMillis() - t1}"
        )
        return modelOutput
    }

    // Resize the given bitmap and convert it to a ByteBuffer
    private fun convertBitmapToBuffer(image: Bitmap): ByteBuffer {
        val scaledImage = Bitmap.createScaledBitmap(image, IMG_SIZE, IMG_SIZE, false)

        val outBuffer = TensorBuffer.createFixedSize(intArrayOf(IMG_SIZE, IMG_SIZE, 3), DataType.FLOAT32)
        outBuffer.loadArray(scaledImage.toFloatArray())
        return imageTensorProcessor.process(outBuffer).buffer
    }

    private fun Bitmap.toFloatArray(): FloatArray {
        var intValues = IntArray(IMG_SIZE * IMG_SIZE)
        getPixels(intValues, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        val resultArray = IntArray(IMG_SIZE * IMG_SIZE * 3)
        var j = 0
        for (i in intValues.indices) {
            resultArray[j++] = intValues[i].shr(16).and(255)
            resultArray[j++] = intValues[i].shr(8).and(255)
            resultArray[j++] = intValues[i].and(255)
        }

        return resultArray.map { it / 255f }.toFloatArray()
    }
}
