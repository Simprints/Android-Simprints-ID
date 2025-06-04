package com.simprints.face.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.infra.config.store.models.FaceConfiguration
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceCaptureParams(
    val samplesToCapture: Int,
    val faceSDK: FaceConfiguration.BioSdk,
) : Parcelable
