package com.simprints.infra.events.event.domain.validators

import com.simprints.infra.events.domain.validators.EventValidator
import com.simprints.infra.events.domain.validators.PersonCreationEventValidator
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import javax.inject.Inject

internal class SessionEventValidatorsFactoryImpl @Inject constructor() :
    SessionEventValidatorsFactory {

    override fun build(): Array<EventValidator> = arrayOf(
        PersonCreationEventValidator(),
        EnrolmentEventValidator()
    )
}
