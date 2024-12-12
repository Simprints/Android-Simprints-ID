package com.simprints.infra.events.exceptions.validator

internal class EnrolmentEventValidatorException(
    message: String = "Saving EnrolmentEvent failed validation",
) : IllegalStateException(message)
