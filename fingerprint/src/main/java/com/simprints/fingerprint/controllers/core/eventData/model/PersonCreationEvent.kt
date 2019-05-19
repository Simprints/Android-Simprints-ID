package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.PersonCreationEvent as CorePersonCreationEvent

@Keep
class PersonCreationEvent(starTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION, starTime)

fun PersonCreationEvent.fromDomainToCore() =
    CorePersonCreationEvent(starTime, fingerprintCaptureIds)
