package com.simprints.infra.events.domain.validators

import javax.inject.Inject

internal class SessionEventValidatorsFactoryImpl @Inject constructor() : SessionEventValidatorsFactory {
    override fun build(): Array<EventValidator> = arrayOf(
        SessionCaptureEventValidator(),
        PersonCreationEventValidator(),
        EnrolmentEventValidator()
    )
}
