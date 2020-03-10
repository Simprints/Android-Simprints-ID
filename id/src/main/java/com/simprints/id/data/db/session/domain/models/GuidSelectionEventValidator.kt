package com.simprints.id.data.db.session.domain.models

import com.simprints.id.data.db.session.domain.models.events.EventType
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException

class GuidSelectionEventValidator: SessionEventValidator {

    override fun validate(session: SessionEvents) {
        if (session.hasEvent(EventType.GUID_SELECTION)) {

            if (session.count(EventType.GUID_SELECTION) > 1)
                throw GuidSelectEventValidatorException("GuidSelectionEvent already exist")

            if (session.count(EventType.CALLBACK_IDENTIFICATION) == 0)
                throw GuidSelectEventValidatorException("Identification Callback missing")
        }
    }
}
