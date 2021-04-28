package com.simprints.id.data.exitform

import com.simprints.id.data.db.event.domain.models.RefusalEvent

enum class FaceExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    APP_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun FaceExitFormReason.toRefusalEventAnswer(): RefusalEvent.RefusalPayload.Answer =
    when(this) {
        FaceExitFormReason.REFUSED_RELIGION -> RefusalEvent.RefusalPayload.Answer.REFUSED_RELIGION
        FaceExitFormReason.REFUSED_DATA_CONCERNS -> RefusalEvent.RefusalPayload.Answer.REFUSED_DATA_CONCERNS
        FaceExitFormReason.REFUSED_PERMISSION -> RefusalEvent.RefusalPayload.Answer.REFUSED_PERMISSION
        FaceExitFormReason.APP_NOT_WORKING -> RefusalEvent.RefusalPayload.Answer.APP_NOT_WORKING
        FaceExitFormReason.REFUSED_NOT_PRESENT -> RefusalEvent.RefusalPayload.Answer.REFUSED_NOT_PRESENT
        FaceExitFormReason.REFUSED_YOUNG -> RefusalEvent.RefusalPayload.Answer.REFUSED_YOUNG
        FaceExitFormReason.OTHER -> RefusalEvent.RefusalPayload.Answer.OTHER
    }
