package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.sample.SampleIdentifier

// **IMPORTANT**: Do NOT change the order of this enum as it is used in the database by index.
// Changing the order of the entries in this enum will lead to data corruption or mismatches
// when retrieving or storing data in the database. Always append new entries at the end.
@Keep
enum class FingerIdentifier {
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

fun FingerIdentifier.fromDomainToModuleApi() = when (this) {
    FingerIdentifier.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
    FingerIdentifier.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
    FingerIdentifier.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
    FingerIdentifier.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
    FingerIdentifier.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
    FingerIdentifier.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
    FingerIdentifier.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
    FingerIdentifier.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
    FingerIdentifier.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
    FingerIdentifier.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
}

fun SampleIdentifier.fromModuleApiToDomain() = when (this) {
    SampleIdentifier.RIGHT_5TH_FINGER -> FingerIdentifier.RIGHT_5TH_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
    SampleIdentifier.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
    else -> null
}
