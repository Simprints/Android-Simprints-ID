package com.simprints.infra.mlkitwrapper.model


class Models {

    companion object {

        val FACENET = ModelInfo(
            name = "FaceNet",
            assetsFilename = "facenet.tflite",
            inputDims = 160,
            outputDims = 128,
            useGpu = true,
        )

        val EDGEFACE = ModelInfo(
            name = "EdgeFace",
            assetsFilename = "edgefacexs.tflite",
            inputDims = 112,
            outputDims = 512,
            useGpu = false,
        )
    }

}
