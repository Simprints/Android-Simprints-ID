package com.simprints.ear.infra.basebiosdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EarSample(
    val faceId: String,
    val template: ByteArray,
) : Parcelable
