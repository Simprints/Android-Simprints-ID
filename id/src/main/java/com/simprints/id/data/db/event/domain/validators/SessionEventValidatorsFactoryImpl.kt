package com.simprints.id.data.db.event.domain.validators

class SessionEventValidatorsFactoryImpl : SessionEventValidatorsFactory {
    override fun build(): Array<EventValidator> = arrayOf(
        GuidSelectionEventValidator(),
        SessionCaptureEventValidator(),
        PersonCreationEventValidator(),
        EnrolmentEventValidator()
    )
}
