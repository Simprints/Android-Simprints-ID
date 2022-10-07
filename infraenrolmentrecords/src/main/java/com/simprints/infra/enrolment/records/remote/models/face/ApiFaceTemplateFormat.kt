package com.simprints.infra.enrolment.records.remote.models.face

import androidx.annotation.Keep
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat

@Keep
internal enum class ApiFaceTemplateFormat {
    RANK_ONE_1_23,
}

internal fun IFaceTemplateFormat.toApi(): ApiFaceTemplateFormat =
    when (this) {
        IFaceTemplateFormat.RANK_ONE_1_23 -> ApiFaceTemplateFormat.RANK_ONE_1_23
        else -> throw IllegalArgumentException("invalid face template format '${this.name}'")
    }
