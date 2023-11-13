package com.simprints.fingerprint.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FingerprintCaptureParams(
  val flowType: FlowType,
  val fingerprintsToCapture: List<IFingerIdentifier>
) : Parcelable
