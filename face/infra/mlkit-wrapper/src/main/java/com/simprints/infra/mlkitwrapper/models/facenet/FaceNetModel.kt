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
package com.simprints.infra.mlkitwrapper.models.facenet

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.simprints.infra.mlkitwrapper.model.MlKitModel
import com.simprints.infra.mlkitwrapper.model.ModelInfo
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.nio.ByteBuffer
import kotlin.apply
import kotlin.collections.average
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.sum
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

// Utility class for FaceNet or EdgeFace model
class FaceNetModel(
    context: Context,
) : MlKitModel {

    companion object {
        private const val ASSET_NAME = "facenet.tflite"

        // Input image size for EdgeFace model.
        private const val IMG_SIZE = 160

        // Output embedding size
        private const val EMBEDDING_SIZE = 128
    }

    private var interpreter: Interpreter
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add(ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(StandardizeOp())
        .build()

    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            // Add the GPU Delegate if supported.
            // See -> https://www.tensorflow.org/lite/performance/gpu#android
            if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                addDelegate(GpuDelegate(CompatibilityList().bestOptionsForThisDevice))
            }
            setUseXNNPACK(true)
            useNNAPI = true
        }
        interpreter = Interpreter(
            FileUtil.loadMappedFile(context, ASSET_NAME),
            interpreterOptions
        )
    }

    // Gets an face embedding using FaceNet.
    override fun getFaceEmbedding(image: Bitmap) = runFaceNet(convertBitmapToBuffer(image))[0]

    // Run the FaceNet model.
    private fun runFaceNet(inputs: Any): Array<FloatArray> {
        val t1 = System.currentTimeMillis()
        val faceNetModelOutputs = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(inputs, faceNetModelOutputs)
        Log.i(
            "Performance",
            "FaceNet Inference Speed in ms : ${System.currentTimeMillis() - t1}"
        )
        return faceNetModelOutputs
    }

    // Resize the given bitmap and convert it to a ByteBuffer
    private fun convertBitmapToBuffer(image: Bitmap): ByteBuffer {
        return imageTensorProcessor.process(TensorImage.fromBitmap(image)).buffer
    }

    // Op to perform standardization
    // x' = ( x - mean ) / std_dev
    class StandardizeOp : TensorOperator {

        override fun apply(p0: TensorBuffer?): TensorBuffer {
            val pixels = p0!!.floatArray
            val mean = pixels.average().toFloat()
            var std = sqrt(pixels.map { pi -> (pi - mean).pow(2) }.sum() / pixels.size.toFloat())
            std = max(std, 1f / sqrt(pixels.size.toFloat()))
            for (i in pixels.indices) {
                pixels[i] = (pixels[i] - mean) / std
            }
            val output = TensorBufferFloat.createFixedSize(p0.shape, DataType.FLOAT32)
            output.loadArray(pixels)
            return output
        }
    }
}
