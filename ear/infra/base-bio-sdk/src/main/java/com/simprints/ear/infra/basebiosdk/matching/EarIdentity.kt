package com.simprints.ear.infra.basebiosdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EarIdentity(
    val subjectId: String,
    val faces: List<EarSample>,
) : Parcelable
