package com.simprints.fingerprint.connect.screens.ota

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.infra.config.store.models.FingerprintConfiguration
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaFragmentParams(
    val fingerprintSDK: FingerprintConfiguration.BioSdk,
    val availableOtas: List<AvailableOta>,
    val currentRetryAttempt: Int = 0,
) : Parcelable
