package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException

class GuidSelectionEventValidator : EventValidator {

    /**
     * You cannot have more than one GUID_SELECTION event, and we require that there is also
     * an CALLBACK_IDENTIFICATION event as well.
     */
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        val currentSession = currentEvents.firstOrNull { it.payload.type == SESSION_CAPTURE }
        if (eventToAdd is GuidSelectionEvent) {

            currentEvents.filter { it.payload.type == GUID_SELECTION }.forEach {
                if (it.id != eventToAdd.id) {
                    throw GuidSelectEventValidatorException("GuidSelectionEvent already exist in session ${currentSession?.id}")
                }
            }

            if (currentEvents.none { it.payload.type == CALLBACK_IDENTIFICATION })
                throw GuidSelectEventValidatorException("Identification Callback missing in session ${currentSession?.id}")
        }
    }
}
