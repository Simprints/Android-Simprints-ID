package com.simprints.face.capture

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceCaptureParams(
    val samplesToCapture: Int,
) : Parcelable
