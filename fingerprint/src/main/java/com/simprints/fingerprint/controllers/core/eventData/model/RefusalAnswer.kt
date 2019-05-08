package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent.Answer as AnswerCore

@Keep
enum class RefusalAnswer {
    BENEFICIARY_REFUSED,
    SCANNER_NOT_WORKING,
    OTHER;
}

fun RefusalAnswer.fromDomainToCore() =
    when(this) {
        RefusalAnswer.BENEFICIARY_REFUSED -> AnswerCore.BENEFICIARY_REFUSED
        RefusalAnswer.SCANNER_NOT_WORKING -> AnswerCore.SCANNER_NOT_WORKING
        RefusalAnswer.OTHER -> AnswerCore.OTHER
    }
