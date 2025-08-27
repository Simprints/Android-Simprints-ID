package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.sample.SampleIdentifier as CoreFingerIdentifier

enum class DbSampleIdentifier(
    val id: Int,
) {
    NONE(-1),

    RIGHT_5TH_FINGER(0),
    RIGHT_4TH_FINGER(1),
    RIGHT_3RD_FINGER(2),
    RIGHT_INDEX_FINGER(3),
    RIGHT_THUMB(4),
    LEFT_THUMB(5),
    LEFT_INDEX_FINGER(6),
    LEFT_3RD_FINGER(7),
    LEFT_4TH_FINGER(8),
    LEFT_5TH_FINGER(9),
    ;

    companion object {
        fun fromId(id: Int?) = DbSampleIdentifier.entries
            .firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Invalid id: $id")
    }
}

internal fun DbSampleIdentifier.toDomain() = when (this) {
    DbSampleIdentifier.NONE -> CoreFingerIdentifier.NONE
    DbSampleIdentifier.RIGHT_5TH_FINGER -> CoreFingerIdentifier.RIGHT_5TH_FINGER
    DbSampleIdentifier.RIGHT_4TH_FINGER -> CoreFingerIdentifier.RIGHT_4TH_FINGER
    DbSampleIdentifier.RIGHT_3RD_FINGER -> CoreFingerIdentifier.RIGHT_3RD_FINGER
    DbSampleIdentifier.RIGHT_INDEX_FINGER -> CoreFingerIdentifier.RIGHT_INDEX_FINGER
    DbSampleIdentifier.RIGHT_THUMB -> CoreFingerIdentifier.RIGHT_THUMB
    DbSampleIdentifier.LEFT_THUMB -> CoreFingerIdentifier.LEFT_THUMB
    DbSampleIdentifier.LEFT_INDEX_FINGER -> CoreFingerIdentifier.LEFT_INDEX_FINGER
    DbSampleIdentifier.LEFT_3RD_FINGER -> CoreFingerIdentifier.LEFT_3RD_FINGER
    DbSampleIdentifier.LEFT_4TH_FINGER -> CoreFingerIdentifier.LEFT_4TH_FINGER
    DbSampleIdentifier.LEFT_5TH_FINGER -> CoreFingerIdentifier.LEFT_5TH_FINGER
}

internal fun CoreFingerIdentifier.fromDomain() = when (this) {
    CoreFingerIdentifier.NONE -> DbSampleIdentifier.NONE
    CoreFingerIdentifier.RIGHT_5TH_FINGER -> DbSampleIdentifier.RIGHT_5TH_FINGER
    CoreFingerIdentifier.RIGHT_4TH_FINGER -> DbSampleIdentifier.RIGHT_4TH_FINGER
    CoreFingerIdentifier.RIGHT_3RD_FINGER -> DbSampleIdentifier.RIGHT_3RD_FINGER
    CoreFingerIdentifier.RIGHT_INDEX_FINGER -> DbSampleIdentifier.RIGHT_INDEX_FINGER
    CoreFingerIdentifier.RIGHT_THUMB -> DbSampleIdentifier.RIGHT_THUMB
    CoreFingerIdentifier.LEFT_THUMB -> DbSampleIdentifier.LEFT_THUMB
    CoreFingerIdentifier.LEFT_INDEX_FINGER -> DbSampleIdentifier.LEFT_INDEX_FINGER
    CoreFingerIdentifier.LEFT_3RD_FINGER -> DbSampleIdentifier.LEFT_3RD_FINGER
    CoreFingerIdentifier.LEFT_4TH_FINGER -> DbSampleIdentifier.LEFT_4TH_FINGER
    CoreFingerIdentifier.LEFT_5TH_FINGER -> DbSampleIdentifier.LEFT_5TH_FINGER
}
