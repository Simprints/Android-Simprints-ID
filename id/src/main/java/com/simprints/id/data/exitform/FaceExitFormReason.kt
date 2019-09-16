package com.simprints.id.data.exitform

import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason

enum class FaceExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun FaceExitFormReason.toRefusalEventAnswer(): RefusalEvent.Answer =
    when(this) {
        FaceExitFormReason.REFUSED_RELIGION -> RefusalEvent.Answer.REFUSED_RELIGION
        FaceExitFormReason.REFUSED_DATA_CONCERNS -> RefusalEvent.Answer.REFUSED_DATA_CONCERNS
        FaceExitFormReason.REFUSED_PERMISSION -> RefusalEvent.Answer.REFUSED_PERMISSION
        FaceExitFormReason.SCANNER_NOT_WORKING -> RefusalEvent.Answer.SCANNER_NOT_WORKING
        FaceExitFormReason.REFUSED_NOT_PRESENT -> RefusalEvent.Answer.REFUSED_NOT_PRESENT
        FaceExitFormReason.REFUSED_YOUNG -> RefusalEvent.Answer.REFUSED_YOUNG
        FaceExitFormReason.OTHER -> RefusalEvent.Answer.OTHER
    }

fun FaceExitFormReason.toAppRefusalFormReason(): RefusalFormReason =
    when (this) {
        FaceExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        FaceExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        FaceExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        FaceExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FaceExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        FaceExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        FaceExitFormReason.OTHER -> RefusalFormReason.OTHER
    }
