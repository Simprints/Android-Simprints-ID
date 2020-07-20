package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent as CorePersonCreationEvent

@Keep
class PersonCreationEvent(
    startTime: Long,
    val fingerprintCaptureIds: List<String>
) : Event(EventType.PERSON_CREATION, startTime)

fun PersonCreationEvent.fromDomainToCore() =
    CorePersonCreationEvent(startTime, fingerprintCaptureIds)
