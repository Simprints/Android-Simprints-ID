package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent

class GuidSelectionEventValidator: SessionEventValidator {

    override fun validate(session: SessionCaptureEvent) {
        //StopShip: To get fixed when Session disappear
//        if (session.hasEvent(EventType.GUID_SELECTION)) {
//
//            if (session.count(EventType.GUID_SELECTION) > 1)
//                throw GuidSelectEventValidatorException("GuidSelectionEvent already exist in session ${session.id}")
//
//            if (session.count(EventType.CALLBACK_IDENTIFICATION) == 0)
//                throw GuidSelectEventValidatorException("Identification Callback missing in session ${session.id}")
//        }
    }
}
