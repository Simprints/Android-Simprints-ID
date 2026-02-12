package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.common.TemplateIdentifier
import kotlinx.serialization.Serializable

/**
 * V1 external schema for sample identifiers.
 * Stable external contract decoupled from internal [TemplateIdentifier].
 */
@Keep
@Serializable
enum class SampleIdentifierV1 {
    NONE,
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

fun TemplateIdentifier.toCoSyncV1(): SampleIdentifierV1 = when (this) {
    TemplateIdentifier.NONE -> SampleIdentifierV1.NONE
    TemplateIdentifier.RIGHT_5TH_FINGER -> SampleIdentifierV1.RIGHT_5TH_FINGER
    TemplateIdentifier.RIGHT_4TH_FINGER -> SampleIdentifierV1.RIGHT_4TH_FINGER
    TemplateIdentifier.RIGHT_3RD_FINGER -> SampleIdentifierV1.RIGHT_3RD_FINGER
    TemplateIdentifier.RIGHT_INDEX_FINGER -> SampleIdentifierV1.RIGHT_INDEX_FINGER
    TemplateIdentifier.RIGHT_THUMB -> SampleIdentifierV1.RIGHT_THUMB
    TemplateIdentifier.LEFT_THUMB -> SampleIdentifierV1.LEFT_THUMB
    TemplateIdentifier.LEFT_INDEX_FINGER -> SampleIdentifierV1.LEFT_INDEX_FINGER
    TemplateIdentifier.LEFT_3RD_FINGER -> SampleIdentifierV1.LEFT_3RD_FINGER
    TemplateIdentifier.LEFT_4TH_FINGER -> SampleIdentifierV1.LEFT_4TH_FINGER
    TemplateIdentifier.LEFT_5TH_FINGER -> SampleIdentifierV1.LEFT_5TH_FINGER
}

fun SampleIdentifierV1.toDomain(): TemplateIdentifier = when (this) {
    SampleIdentifierV1.NONE -> TemplateIdentifier.NONE
    SampleIdentifierV1.RIGHT_5TH_FINGER -> TemplateIdentifier.RIGHT_5TH_FINGER
    SampleIdentifierV1.RIGHT_4TH_FINGER -> TemplateIdentifier.RIGHT_4TH_FINGER
    SampleIdentifierV1.RIGHT_3RD_FINGER -> TemplateIdentifier.RIGHT_3RD_FINGER
    SampleIdentifierV1.RIGHT_INDEX_FINGER -> TemplateIdentifier.RIGHT_INDEX_FINGER
    SampleIdentifierV1.RIGHT_THUMB -> TemplateIdentifier.RIGHT_THUMB
    SampleIdentifierV1.LEFT_THUMB -> TemplateIdentifier.LEFT_THUMB
    SampleIdentifierV1.LEFT_INDEX_FINGER -> TemplateIdentifier.LEFT_INDEX_FINGER
    SampleIdentifierV1.LEFT_3RD_FINGER -> TemplateIdentifier.LEFT_3RD_FINGER
    SampleIdentifierV1.LEFT_4TH_FINGER -> TemplateIdentifier.LEFT_4TH_FINGER
    SampleIdentifierV1.LEFT_5TH_FINGER -> TemplateIdentifier.LEFT_5TH_FINGER
}
