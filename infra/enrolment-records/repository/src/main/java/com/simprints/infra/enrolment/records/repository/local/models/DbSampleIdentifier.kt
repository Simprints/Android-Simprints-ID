package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.sample.SampleIdentifier

enum class DbSampleIdentifier(
    val id: Int,
) {
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

    companion object Companion {
        fun fromId(id: Int?) = DbSampleIdentifier.entries.firstOrNull { it.id == id }
    }
}

internal fun DbSampleIdentifier?.toDomain() = when (this) {
    null -> SampleIdentifier.NONE
    DbSampleIdentifier.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
    DbSampleIdentifier.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
    DbSampleIdentifier.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
    DbSampleIdentifier.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
    DbSampleIdentifier.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
    DbSampleIdentifier.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
    DbSampleIdentifier.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
    DbSampleIdentifier.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
    DbSampleIdentifier.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
    DbSampleIdentifier.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
}

internal fun SampleIdentifier.fromDomain() = when (this) {
    SampleIdentifier.NONE -> null
    SampleIdentifier.RIGHT_5TH_FINGER -> DbSampleIdentifier.RIGHT_5TH_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> DbSampleIdentifier.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> DbSampleIdentifier.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_INDEX_FINGER -> DbSampleIdentifier.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_THUMB -> DbSampleIdentifier.RIGHT_THUMB
    SampleIdentifier.LEFT_THUMB -> DbSampleIdentifier.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> DbSampleIdentifier.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> DbSampleIdentifier.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> DbSampleIdentifier.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> DbSampleIdentifier.LEFT_5TH_FINGER
}
