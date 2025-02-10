package com.simprints.infra.events.event.domain.validators

import javax.inject.Inject

internal class SessionEventValidatorsFactory @Inject constructor() {
    fun build(): Array<EventValidator> = arrayOf(
        EnrolmentEventValidator(),
    )
}
