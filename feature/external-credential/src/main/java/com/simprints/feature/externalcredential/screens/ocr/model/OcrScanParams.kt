package com.simprints.feature.externalcredential.screens.ocr.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class OcrScanParams(
    val imagePath: String,
    val ocrParams: OcrParams
) : Parcelable
