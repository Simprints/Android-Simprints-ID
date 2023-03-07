package com.simprints.infra.events.exceptions.validator

import com.simprints.core.exceptions.UnexpectedException

internal class EnrolmentEventValidatorException(
    message: String = "Saving EnrolmentEvent failed validation"
): UnexpectedException(message)
