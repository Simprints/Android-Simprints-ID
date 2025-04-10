package com.simprints.infra.enrolment.records.repository.local.models
import com.simprints.core.domain.fingerprint.IFingerIdentifier as CoreFingerIdentifier

enum class IFingerIdentifier(
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

    // create from id
    companion object {
        fun fromId(id: Int) = IFingerIdentifier.entries
            .firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Invalid id: $id")
    }
}

// convert to CoreFingerIdentifier
internal fun IFingerIdentifier.toDomain() = when (this) {
    IFingerIdentifier.RIGHT_5TH_FINGER -> CoreFingerIdentifier.RIGHT_5TH_FINGER
    IFingerIdentifier.RIGHT_4TH_FINGER -> CoreFingerIdentifier.RIGHT_4TH_FINGER
    IFingerIdentifier.RIGHT_3RD_FINGER -> CoreFingerIdentifier.RIGHT_3RD_FINGER
    IFingerIdentifier.RIGHT_INDEX_FINGER -> CoreFingerIdentifier.RIGHT_INDEX_FINGER
    IFingerIdentifier.RIGHT_THUMB -> CoreFingerIdentifier.RIGHT_THUMB
    IFingerIdentifier.LEFT_THUMB -> CoreFingerIdentifier.LEFT_THUMB
    IFingerIdentifier.LEFT_INDEX_FINGER -> CoreFingerIdentifier.LEFT_INDEX_FINGER
    IFingerIdentifier.LEFT_3RD_FINGER -> CoreFingerIdentifier.LEFT_3RD_FINGER
    IFingerIdentifier.LEFT_4TH_FINGER -> CoreFingerIdentifier.LEFT_4TH_FINGER
    IFingerIdentifier.LEFT_5TH_FINGER -> CoreFingerIdentifier.LEFT_5TH_FINGER
}

// convert from CoreFingerIdentifier
internal fun CoreFingerIdentifier.fromDomain() = when (this) {
    CoreFingerIdentifier.RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
    CoreFingerIdentifier.RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
    CoreFingerIdentifier.RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
    CoreFingerIdentifier.RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
    CoreFingerIdentifier.RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
    CoreFingerIdentifier.LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
    CoreFingerIdentifier.LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
    CoreFingerIdentifier.LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
    CoreFingerIdentifier.LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
    CoreFingerIdentifier.LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
}
