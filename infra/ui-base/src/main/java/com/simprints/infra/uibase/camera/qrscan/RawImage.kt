package com.simprints.infra.uibase.camera.qrscan

import android.media.Image

data class RawImage(
    val image: Image,
    val rotationDegrees: Int,
)
