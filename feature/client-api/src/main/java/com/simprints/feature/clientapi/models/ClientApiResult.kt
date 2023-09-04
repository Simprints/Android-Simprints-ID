package com.simprints.feature.clientapi.models

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientApiResult(
    val resultCode: Int,
    val extras: Bundle,
) : Parcelable
