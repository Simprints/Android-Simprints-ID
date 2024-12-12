package com.simprints.fingerprint.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.config.store.models.FingerprintConfiguration
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FingerprintCaptureParams(
    val flowType: FlowType,
    val fingerprintsToCapture: List<IFingerIdentifier>,
    val fingerprintSDK: FingerprintConfiguration.BioSdk,
) : Parcelable
