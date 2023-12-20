package com.simprints.feature.setup

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SetupResult(
    val permissionGranted: Boolean
) : Serializable
