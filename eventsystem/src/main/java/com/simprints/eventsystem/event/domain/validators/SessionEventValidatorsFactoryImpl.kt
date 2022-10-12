package com.simprints.eventsystem.event.domain.validators

import javax.inject.Inject

class SessionEventValidatorsFactoryImpl @Inject constructor() : SessionEventValidatorsFactory {
    override fun build(): Array<EventValidator> = arrayOf(
        SessionCaptureEventValidator(),
        PersonCreationEventValidator(),
        EnrolmentEventValidator()
    )
}
