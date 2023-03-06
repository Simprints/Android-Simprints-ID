package com.simprints.infra.events.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
data class ApiFingerprintTemplate(val quality: Int,
                                  val template: String,
                                  val finger: IFingerIdentifier
)

