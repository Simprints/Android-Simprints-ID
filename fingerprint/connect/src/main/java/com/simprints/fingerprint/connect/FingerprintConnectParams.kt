package com.simprints.fingerprint.connect

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.infra.config.store.models.FingerprintConfiguration
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FingerprintConnectParams(
    val fingerprintSDK: FingerprintConfiguration.BioSdk,
) : Parcelable
