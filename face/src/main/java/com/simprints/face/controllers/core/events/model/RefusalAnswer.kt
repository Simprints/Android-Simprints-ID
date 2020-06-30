package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.RefusalEvent
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

    fun fromDomainToCore(): RefusalEvent.Answer =
        when (this) {
            REFUSED_RELIGION -> RefusalEvent.Answer.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> RefusalEvent.Answer.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> RefusalEvent.Answer.REFUSED_PERMISSION
            APP_NOT_WORKING -> RefusalEvent.Answer.OTHER // TODO: Map to correct Hawkeye APP_NOT_WORKING when ready
            REFUSED_NOT_PRESENT -> RefusalEvent.Answer.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> RefusalEvent.Answer.REFUSED_YOUNG
            OTHER -> RefusalEvent.Answer.OTHER
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
