package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.sample.SampleIdentifier

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

internal fun SampleIdentifier.toApi(): ApiFinger = when (this) {
    SampleIdentifier.RIGHT_5TH_FINGER -> ApiFinger.RIGHT_5TH_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> ApiFinger.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> ApiFinger.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_INDEX_FINGER -> ApiFinger.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_THUMB -> ApiFinger.RIGHT_THUMB
    SampleIdentifier.LEFT_THUMB -> ApiFinger.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> ApiFinger.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> ApiFinger.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> ApiFinger.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> ApiFinger.LEFT_5TH_FINGER
    SampleIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
}
