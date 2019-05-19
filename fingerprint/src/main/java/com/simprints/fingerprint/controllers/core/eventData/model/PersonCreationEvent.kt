package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.PersonCreationEvent as CorePersonCreationEvent

@Keep
class PersonCreationEvent(override val starTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION)

fun PersonCreationEvent.fromDomainToCore() =
    CorePersonCreationEvent(starTime, fingerprintCaptureIds)
