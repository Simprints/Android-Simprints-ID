package com.simprints.fingerprint.connect.screens.ota.recovery

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaRecoveryParams(
    val fingerprintSDK: ModalitySdkType,
    val remainingOtas: List<AvailableOta>,
    val currentRetryAttempt: Int,
    val recoveryStrategy: OtaRecoveryStrategy,
) : Parcelable
