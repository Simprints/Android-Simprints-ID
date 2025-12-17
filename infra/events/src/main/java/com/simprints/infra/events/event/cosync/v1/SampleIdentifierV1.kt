package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.sample.SampleIdentifier

/**
 * V1 external schema for sample identifiers.
 *
 * Represents which finger a fingerprint sample belongs to.
 * This is a stable external contract decoupled from internal domain models.
 */
@Keep
enum class SampleIdentifierV1 {
    NONE,

    // Fingerprint specific identifiers
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

/**
 * Converts internal SampleIdentifier to V1 external schema.
 */
fun SampleIdentifier.toCoSyncV1(): SampleIdentifierV1 = when (this) {
    SampleIdentifier.NONE -> SampleIdentifierV1.NONE
    SampleIdentifier.RIGHT_5TH_FINGER -> SampleIdentifierV1.RIGHT_5TH_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> SampleIdentifierV1.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> SampleIdentifierV1.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_INDEX_FINGER -> SampleIdentifierV1.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_THUMB -> SampleIdentifierV1.RIGHT_THUMB
    SampleIdentifier.LEFT_THUMB -> SampleIdentifierV1.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> SampleIdentifierV1.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> SampleIdentifierV1.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> SampleIdentifierV1.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> SampleIdentifierV1.LEFT_5TH_FINGER
}

/**
 * Converts V1 external schema to internal SampleIdentifier.
 */
fun SampleIdentifierV1.toDomain(): SampleIdentifier = when (this) {
    SampleIdentifierV1.NONE -> SampleIdentifier.NONE
    SampleIdentifierV1.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
    SampleIdentifierV1.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
    SampleIdentifierV1.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
    SampleIdentifierV1.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
    SampleIdentifierV1.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
    SampleIdentifierV1.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
    SampleIdentifierV1.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
    SampleIdentifierV1.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
    SampleIdentifierV1.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
    SampleIdentifierV1.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
}
