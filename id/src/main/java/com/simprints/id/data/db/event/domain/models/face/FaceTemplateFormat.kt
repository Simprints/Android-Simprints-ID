package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat

@Keep
enum class FaceTemplateFormat {
    RANK_ONE_1_23,
    MOCK;

    fun fromDomainToModuleApi(): IFaceTemplateFormat =
        when (this) {
            RANK_ONE_1_23 -> IFaceTemplateFormat.RANK_ONE_1_23
            MOCK -> IFaceTemplateFormat.MOCK
        }
}

fun IFaceTemplateFormat.fromModuleApiToDomain(): FaceTemplateFormat =
    when (this) {
        IFaceTemplateFormat.RANK_ONE_1_23 -> FaceTemplateFormat.RANK_ONE_1_23
        IFaceTemplateFormat.MOCK -> FaceTemplateFormat.MOCK
    }
