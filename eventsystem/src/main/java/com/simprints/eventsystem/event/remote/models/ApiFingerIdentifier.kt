package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.subject.FingerIdentifier
import com.simprints.eventsystem.event.domain.models.subject.FingerIdentifier.*

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

fun FingerIdentifier.fromDomainToApi() =
    when(this) {
        RIGHT_5TH_FINGER -> ApiFingerIdentifier.RIGHT_5TH_FINGER
        RIGHT_4TH_FINGER -> ApiFingerIdentifier.RIGHT_4TH_FINGER
        RIGHT_3RD_FINGER -> ApiFingerIdentifier.RIGHT_3RD_FINGER
        RIGHT_INDEX_FINGER -> ApiFingerIdentifier.RIGHT_INDEX_FINGER
        RIGHT_THUMB ->ApiFingerIdentifier.RIGHT_THUMB
        LEFT_THUMB -> ApiFingerIdentifier.LEFT_THUMB
        LEFT_INDEX_FINGER -> ApiFingerIdentifier.LEFT_INDEX_FINGER
        LEFT_3RD_FINGER -> ApiFingerIdentifier.LEFT_3RD_FINGER
        LEFT_4TH_FINGER -> ApiFingerIdentifier.LEFT_4TH_FINGER
        LEFT_5TH_FINGER -> ApiFingerIdentifier.LEFT_5TH_FINGER
    }
