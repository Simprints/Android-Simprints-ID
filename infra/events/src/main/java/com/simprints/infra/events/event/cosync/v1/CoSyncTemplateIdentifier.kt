package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.TemplateIdentifier
import kotlinx.serialization.Serializable

/**
 * V1 external schema for template identifiers.
 * Stable external contract decoupled from internal [TemplateIdentifier].
 */
@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Enum")
enum class CoSyncTemplateIdentifier {
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

fun TemplateIdentifier.toCoSync(): CoSyncTemplateIdentifier = when (this) {
    TemplateIdentifier.NONE -> CoSyncTemplateIdentifier.NONE
    TemplateIdentifier.RIGHT_5TH_FINGER -> CoSyncTemplateIdentifier.RIGHT_5TH_FINGER
    TemplateIdentifier.RIGHT_4TH_FINGER -> CoSyncTemplateIdentifier.RIGHT_4TH_FINGER
    TemplateIdentifier.RIGHT_3RD_FINGER -> CoSyncTemplateIdentifier.RIGHT_3RD_FINGER
    TemplateIdentifier.RIGHT_INDEX_FINGER -> CoSyncTemplateIdentifier.RIGHT_INDEX_FINGER
    TemplateIdentifier.RIGHT_THUMB -> CoSyncTemplateIdentifier.RIGHT_THUMB
    TemplateIdentifier.LEFT_THUMB -> CoSyncTemplateIdentifier.LEFT_THUMB
    TemplateIdentifier.LEFT_INDEX_FINGER -> CoSyncTemplateIdentifier.LEFT_INDEX_FINGER
    TemplateIdentifier.LEFT_3RD_FINGER -> CoSyncTemplateIdentifier.LEFT_3RD_FINGER
    TemplateIdentifier.LEFT_4TH_FINGER -> CoSyncTemplateIdentifier.LEFT_4TH_FINGER
    TemplateIdentifier.LEFT_5TH_FINGER -> CoSyncTemplateIdentifier.LEFT_5TH_FINGER
}

fun CoSyncTemplateIdentifier.toDomain(): TemplateIdentifier = when (this) {
    CoSyncTemplateIdentifier.NONE -> TemplateIdentifier.NONE
    CoSyncTemplateIdentifier.RIGHT_5TH_FINGER -> TemplateIdentifier.RIGHT_5TH_FINGER
    CoSyncTemplateIdentifier.RIGHT_4TH_FINGER -> TemplateIdentifier.RIGHT_4TH_FINGER
    CoSyncTemplateIdentifier.RIGHT_3RD_FINGER -> TemplateIdentifier.RIGHT_3RD_FINGER
    CoSyncTemplateIdentifier.RIGHT_INDEX_FINGER -> TemplateIdentifier.RIGHT_INDEX_FINGER
    CoSyncTemplateIdentifier.RIGHT_THUMB -> TemplateIdentifier.RIGHT_THUMB
    CoSyncTemplateIdentifier.LEFT_THUMB -> TemplateIdentifier.LEFT_THUMB
    CoSyncTemplateIdentifier.LEFT_INDEX_FINGER -> TemplateIdentifier.LEFT_INDEX_FINGER
    CoSyncTemplateIdentifier.LEFT_3RD_FINGER -> TemplateIdentifier.LEFT_3RD_FINGER
    CoSyncTemplateIdentifier.LEFT_4TH_FINGER -> TemplateIdentifier.LEFT_4TH_FINGER
    CoSyncTemplateIdentifier.LEFT_5TH_FINGER -> TemplateIdentifier.LEFT_5TH_FINGER
}
