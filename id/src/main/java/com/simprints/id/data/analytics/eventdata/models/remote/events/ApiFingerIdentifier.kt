package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.FingerIdentifier

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

fun FingerIdentifier.toApiFingerIdentifier() =
    when(this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> ApiFingerIdentifier.RIGHT_5TH_FINGER
        FingerIdentifier.RIGHT_4TH_FINGER -> ApiFingerIdentifier.RIGHT_4TH_FINGER
        FingerIdentifier.RIGHT_3RD_FINGER -> ApiFingerIdentifier.RIGHT_3RD_FINGER
        FingerIdentifier.RIGHT_INDEX_FINGER -> ApiFingerIdentifier.RIGHT_INDEX_FINGER
        FingerIdentifier.RIGHT_THUMB ->ApiFingerIdentifier.RIGHT_THUMB
        FingerIdentifier.LEFT_THUMB -> ApiFingerIdentifier.LEFT_THUMB
        FingerIdentifier.LEFT_INDEX_FINGER -> ApiFingerIdentifier.LEFT_INDEX_FINGER
        FingerIdentifier.LEFT_3RD_FINGER -> ApiFingerIdentifier.LEFT_3RD_FINGER
        FingerIdentifier.LEFT_4TH_FINGER -> ApiFingerIdentifier.LEFT_4TH_FINGER
        FingerIdentifier.LEFT_5TH_FINGER -> ApiFingerIdentifier.LEFT_5TH_FINGER
    }
