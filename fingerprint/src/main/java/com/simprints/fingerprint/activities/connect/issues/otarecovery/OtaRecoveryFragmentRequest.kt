package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Parcelable
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtaRecoveryFragmentRequest(val recoveryStrategy: OtaRecoveryStrategy,
                                      val remainingOtas: List<AvailableOta>,
                                      val currentRetryAttempt: Int) : Parcelable
