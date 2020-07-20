package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent as CorePersonCreationEvent

@Keep
class PersonCreationEvent(startTime: Long,
                          val faceCaptureIds: List<String>) : Event(EventType.PERSON_CREATION, startTime) {
    fun fromDomainToCore() = CorePersonCreationEvent(startTime, faceCaptureIds)
}


