package com.simprints.fingerprint.connect.screens.ota

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaFragmentParams(
    val availableOtas: List<AvailableOta>,
    val currentRetryAttempt: Int = 0
) : Parcelable
