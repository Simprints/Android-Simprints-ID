package com.simprints.eventsystem.event.domain.validators

class SessionEventValidatorsFactoryImpl : SessionEventValidatorsFactory {
    override fun build(): Array<EventValidator> = arrayOf(
        GuidSelectionEventValidator(),
        SessionCaptureEventValidator(),
        PersonCreationEventValidator(),
        EnrolmentEventValidator()
    )
}
