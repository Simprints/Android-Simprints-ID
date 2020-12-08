package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent as CoreAlertScreenEvent

@Keep
class AlertScreenEvent(
    startTime: Long,
    private val faceAlertType: FaceAlertType
) : Event(EventType.ALERT_SCREEN, startTime) {
    fun fromDomainToCore() = CoreAlertScreenEvent(startTime, faceAlertType.fromDomainToCore())
}


