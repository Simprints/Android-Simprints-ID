package com.simprints.infra.mlkitwrapper.model

import android.graphics.Bitmap

interface MlKitModel {
    fun getFaceEmbedding(image: Bitmap): FloatArray
}
