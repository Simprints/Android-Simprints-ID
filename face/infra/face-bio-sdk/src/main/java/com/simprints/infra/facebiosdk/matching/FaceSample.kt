package com.simprints.infra.facebiosdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceSample(val faceId: String, val template: ByteArray) : Parcelable
