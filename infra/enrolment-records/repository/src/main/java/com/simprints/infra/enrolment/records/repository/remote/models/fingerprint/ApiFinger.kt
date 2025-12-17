package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.reference.TemplateIdentifier

@Keep
internal enum class ApiFinger {
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

internal fun TemplateIdentifier.toApi(): ApiFinger = when (this) {
    TemplateIdentifier.RIGHT_5TH_FINGER -> ApiFinger.RIGHT_5TH_FINGER
    TemplateIdentifier.RIGHT_4TH_FINGER -> ApiFinger.RIGHT_4TH_FINGER
    TemplateIdentifier.RIGHT_3RD_FINGER -> ApiFinger.RIGHT_3RD_FINGER
    TemplateIdentifier.RIGHT_INDEX_FINGER -> ApiFinger.RIGHT_INDEX_FINGER
    TemplateIdentifier.RIGHT_THUMB -> ApiFinger.RIGHT_THUMB
    TemplateIdentifier.LEFT_THUMB -> ApiFinger.LEFT_THUMB
    TemplateIdentifier.LEFT_INDEX_FINGER -> ApiFinger.LEFT_INDEX_FINGER
    TemplateIdentifier.LEFT_3RD_FINGER -> ApiFinger.LEFT_3RD_FINGER
    TemplateIdentifier.LEFT_4TH_FINGER -> ApiFinger.LEFT_4TH_FINGER
    TemplateIdentifier.LEFT_5TH_FINGER -> ApiFinger.LEFT_5TH_FINGER
    TemplateIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
}
