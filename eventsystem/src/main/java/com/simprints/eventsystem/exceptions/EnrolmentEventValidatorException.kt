package com.simprints.eventsystem.exceptions

import com.simprints.core.exceptions.UnexpectedException

class EnrolmentEventValidatorException(message: String = "Saving EnrolmentEvent failed validation"): UnexpectedException(message)
