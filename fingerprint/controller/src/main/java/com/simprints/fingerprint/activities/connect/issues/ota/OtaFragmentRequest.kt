package com.simprints.fingerprint.activities.connect.issues.ota

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaFragmentRequest(val availableOtas: List<AvailableOta>,
                              val currentRetryAttempt: Int = 0) : Parcelable
