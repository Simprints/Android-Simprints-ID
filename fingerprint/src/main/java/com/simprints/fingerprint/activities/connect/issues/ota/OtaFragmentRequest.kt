package com.simprints.fingerprint.activities.connect.issues.ota

import android.os.Parcelable
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtaFragmentRequest(val availableOtas: List<AvailableOta>,
                              val currentRetryAttempt: Int = 0) : Parcelable
