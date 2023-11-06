package com.simprints.fingerprint.connect

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FingerprintConnectParams(
    val isReconnect: Boolean
) : Parcelable
