package com.simprints.fingerprint.connect.screens.ota.recovery

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaRecoveryParams(
    val recoveryStrategy: OtaRecoveryStrategy,
    val remainingOtas: List<AvailableOta>,
    val currentRetryAttempt: Int
) : Parcelable
