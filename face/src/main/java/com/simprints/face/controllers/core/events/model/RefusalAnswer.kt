package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer
import com.simprints.moduleapi.face.responses.IFaceExitReason

@Keep
enum class RefusalAnswer {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    APP_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER;

    fun fromDomainToCore(): Answer =
        when (this) {
            REFUSED_RELIGION -> Answer.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> Answer.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> Answer.REFUSED_PERMISSION
            APP_NOT_WORKING -> Answer.OTHER // TODO: Map to correct Hawkeye APP_NOT_WORKING when ready
            REFUSED_NOT_PRESENT -> Answer.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> Answer.REFUSED_YOUNG
            OTHER -> Answer.OTHER
        }

    fun fromDomainToExitReason(): IFaceExitReason =
        when (this) {
            REFUSED_RELIGION -> IFaceExitReason.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> IFaceExitReason.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> IFaceExitReason.REFUSED_PERMISSION
            APP_NOT_WORKING -> IFaceExitReason.APP_NOT_WORKING
            REFUSED_NOT_PRESENT -> IFaceExitReason.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> IFaceExitReason.REFUSED_YOUNG
            OTHER -> IFaceExitReason.OTHER
        }

}
