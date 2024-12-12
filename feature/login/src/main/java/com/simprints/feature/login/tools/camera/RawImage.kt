package com.simprints.feature.login.tools.camera

import android.media.Image

internal data class RawImage(
    val image: Image,
    val rotationDegrees: Int,
)
