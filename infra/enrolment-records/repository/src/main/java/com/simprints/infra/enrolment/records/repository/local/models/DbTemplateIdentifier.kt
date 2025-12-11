package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.reference.TemplateIdentifier

enum class DbTemplateIdentifier(
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
        fun fromId(id: Int?) = DbTemplateIdentifier.entries.firstOrNull { it.id == id }
    }
}

internal fun DbTemplateIdentifier?.toDomain() = when (this) {
    null -> TemplateIdentifier.NONE
    DbTemplateIdentifier.RIGHT_5TH_FINGER -> TemplateIdentifier.RIGHT_5TH_FINGER
    DbTemplateIdentifier.RIGHT_4TH_FINGER -> TemplateIdentifier.RIGHT_4TH_FINGER
    DbTemplateIdentifier.RIGHT_3RD_FINGER -> TemplateIdentifier.RIGHT_3RD_FINGER
    DbTemplateIdentifier.RIGHT_INDEX_FINGER -> TemplateIdentifier.RIGHT_INDEX_FINGER
    DbTemplateIdentifier.RIGHT_THUMB -> TemplateIdentifier.RIGHT_THUMB
    DbTemplateIdentifier.LEFT_THUMB -> TemplateIdentifier.LEFT_THUMB
    DbTemplateIdentifier.LEFT_INDEX_FINGER -> TemplateIdentifier.LEFT_INDEX_FINGER
    DbTemplateIdentifier.LEFT_3RD_FINGER -> TemplateIdentifier.LEFT_3RD_FINGER
    DbTemplateIdentifier.LEFT_4TH_FINGER -> TemplateIdentifier.LEFT_4TH_FINGER
    DbTemplateIdentifier.LEFT_5TH_FINGER -> TemplateIdentifier.LEFT_5TH_FINGER
}

internal fun TemplateIdentifier.fromDomain() = when (this) {
    TemplateIdentifier.NONE -> null
    TemplateIdentifier.RIGHT_5TH_FINGER -> DbTemplateIdentifier.RIGHT_5TH_FINGER
    TemplateIdentifier.RIGHT_4TH_FINGER -> DbTemplateIdentifier.RIGHT_4TH_FINGER
    TemplateIdentifier.RIGHT_3RD_FINGER -> DbTemplateIdentifier.RIGHT_3RD_FINGER
    TemplateIdentifier.RIGHT_INDEX_FINGER -> DbTemplateIdentifier.RIGHT_INDEX_FINGER
    TemplateIdentifier.RIGHT_THUMB -> DbTemplateIdentifier.RIGHT_THUMB
    TemplateIdentifier.LEFT_THUMB -> DbTemplateIdentifier.LEFT_THUMB
    TemplateIdentifier.LEFT_INDEX_FINGER -> DbTemplateIdentifier.LEFT_INDEX_FINGER
    TemplateIdentifier.LEFT_3RD_FINGER -> DbTemplateIdentifier.LEFT_3RD_FINGER
    TemplateIdentifier.LEFT_4TH_FINGER -> DbTemplateIdentifier.LEFT_4TH_FINGER
    TemplateIdentifier.LEFT_5TH_FINGER -> DbTemplateIdentifier.LEFT_5TH_FINGER
}
