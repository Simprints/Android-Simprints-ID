package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
data class FingerprintTemplate(val quality: Int,
                               val template: String,
                               val finger: IFingerIdentifier
)



fun ApiFingerprintTemplate.fromApiToDomain() =
    FingerprintTemplate(quality, template, IFingerIdentifier.valueOf(finger.name))


