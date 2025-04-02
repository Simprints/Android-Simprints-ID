package com.simprints.feature.externalcredential.tools.camera

import android.media.Image

internal data class RawImage(
    val image: Image,
    val rotationDegrees: Int,
)
