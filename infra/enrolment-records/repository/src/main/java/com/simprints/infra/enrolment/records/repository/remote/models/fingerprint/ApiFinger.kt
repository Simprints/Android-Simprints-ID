package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier

@Keep
internal enum class ApiFinger {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER,
}

internal fun IFingerIdentifier.toApi(): ApiFinger = when (this) {
    IFingerIdentifier.RIGHT_5TH_FINGER -> ApiFinger.RIGHT_5TH_FINGER
    IFingerIdentifier.RIGHT_4TH_FINGER -> ApiFinger.RIGHT_4TH_FINGER
    IFingerIdentifier.RIGHT_3RD_FINGER -> ApiFinger.RIGHT_3RD_FINGER
    IFingerIdentifier.RIGHT_INDEX_FINGER -> ApiFinger.RIGHT_INDEX_FINGER
    IFingerIdentifier.RIGHT_THUMB -> ApiFinger.RIGHT_THUMB
    IFingerIdentifier.LEFT_THUMB -> ApiFinger.LEFT_THUMB
    IFingerIdentifier.LEFT_INDEX_FINGER -> ApiFinger.LEFT_INDEX_FINGER
    IFingerIdentifier.LEFT_3RD_FINGER -> ApiFinger.LEFT_3RD_FINGER
    IFingerIdentifier.LEFT_4TH_FINGER -> ApiFinger.LEFT_4TH_FINGER
    IFingerIdentifier.LEFT_5TH_FINGER -> ApiFinger.LEFT_5TH_FINGER
}
