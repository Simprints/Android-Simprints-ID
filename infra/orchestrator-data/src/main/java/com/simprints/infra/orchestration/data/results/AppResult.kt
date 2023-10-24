package com.simprints.infra.orchestration.data.results

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppResult(
    val resultCode: Int,
    val extras: Bundle,
) : Parcelable
