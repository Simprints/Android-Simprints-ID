package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.infra.events.event.domain.models.RefusalEvent as CoreRefusalEvent
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer as CoreRefusalAnswer

/**
 * This class represents a event from submission of a Refusal form.
 *
 * @property reason  the reason for refusing a fingerprint capture
 * @property otherText  other written information, submitted with the form
 */
@Keep
class RefusalEvent(startTime: Long,
                   endTime: Long,
                   val reason: RefusalAnswer,
                   val otherText: String) : Event(EventType.REFUSAL, startTime, endTime)

/**
 * This enum class represents the different reasons for refusing a fingerprint capture
 */
@Keep
enum class RefusalAnswer {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    APP_NOT_WORKING,
    REFUSED_YOUNG,
    OTHER;

    companion object {
        fun fromRefusalFormReason(refusalFormReason: RefusalFormReason) =
            when (refusalFormReason) {
                RefusalFormReason.REFUSED_RELIGION -> REFUSED_RELIGION
                RefusalFormReason.REFUSED_DATA_CONCERNS -> REFUSED_DATA_CONCERNS
                RefusalFormReason.REFUSED_PERMISSION -> REFUSED_PERMISSION
                RefusalFormReason.SCANNER_NOT_WORKING -> SCANNER_NOT_WORKING
                RefusalFormReason.REFUSED_NOT_PRESENT -> REFUSED_NOT_PRESENT
                RefusalFormReason.REFUSED_YOUNG -> REFUSED_YOUNG
                RefusalFormReason.APP_NOT_WORKING -> APP_NOT_WORKING
                RefusalFormReason.OTHER -> OTHER
            }
    }
}

fun RefusalEvent.fromDomainToCore() =
    CoreRefusalEvent(startTime, endTime, reason.fromDomainToCore(), otherText)

fun RefusalAnswer.fromDomainToCore(): CoreRefusalAnswer =
    when (this) {
        RefusalAnswer.REFUSED_RELIGION -> CoreRefusalAnswer.REFUSED_RELIGION
        RefusalAnswer.REFUSED_DATA_CONCERNS -> CoreRefusalAnswer.REFUSED_DATA_CONCERNS
        RefusalAnswer.REFUSED_PERMISSION -> CoreRefusalAnswer.REFUSED_PERMISSION
        RefusalAnswer.SCANNER_NOT_WORKING -> CoreRefusalAnswer.SCANNER_NOT_WORKING
        RefusalAnswer.REFUSED_NOT_PRESENT -> CoreRefusalAnswer.REFUSED_NOT_PRESENT
        RefusalAnswer.REFUSED_YOUNG -> CoreRefusalAnswer.REFUSED_YOUNG
        RefusalAnswer.APP_NOT_WORKING -> CoreRefusalAnswer.APP_NOT_WORKING
        RefusalAnswer.OTHER -> CoreRefusalAnswer.OTHER
    }
