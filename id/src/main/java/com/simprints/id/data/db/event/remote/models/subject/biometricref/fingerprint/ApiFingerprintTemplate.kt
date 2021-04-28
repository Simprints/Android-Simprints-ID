package com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep

@Keep
data class ApiFingerprintTemplate(val quality: Int,
                                  val template: String,
                                  val finger: ApiFingerIdentifier)

@Keep
enum class ApiFingerIdentifier {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER
}
