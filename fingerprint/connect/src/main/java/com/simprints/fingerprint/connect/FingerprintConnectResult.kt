package com.simprints.fingerprint.connect

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class FingerprintConnectResult(
    val isSuccess: Boolean,
) : Serializable
