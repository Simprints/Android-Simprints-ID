package com.simprints.fingerprint.connect.screens.ota

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaFragmentParams(
    val fingerprintSDK: ModalitySdkType,
    val availableOtas: List<AvailableOta>,
    val currentRetryAttempt: Int = 0,
) : Parcelable
