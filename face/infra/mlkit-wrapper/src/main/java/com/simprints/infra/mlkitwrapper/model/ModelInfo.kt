package com.simprints.infra.mlkitwrapper.model

class ModelInfo(
    val name: String,
    val assetsFilename: String,
    val outputDims: Int,
    val inputDims: Int,
    val useGpu: Boolean,
)
