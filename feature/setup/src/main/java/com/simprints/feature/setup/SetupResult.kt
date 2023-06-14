package com.simprints.feature.setup

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SetupResult(
    val permissionGranted: Boolean,
) : Parcelable
