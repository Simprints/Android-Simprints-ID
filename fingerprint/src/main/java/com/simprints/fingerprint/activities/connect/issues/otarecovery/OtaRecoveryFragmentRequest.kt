package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class OtaRecoveryFragmentRequest(val recoveryStrategy: OtaRecoveryStrategy,
                                      val remainingOtas: List<AvailableOta>,
                                      val currentRetryAttempt: Int) : Parcelable
