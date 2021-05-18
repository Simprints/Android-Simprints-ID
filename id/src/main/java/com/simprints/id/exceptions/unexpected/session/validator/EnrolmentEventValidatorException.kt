package com.simprints.id.exceptions.unexpected.session.validator

import com.simprints.core.exceptions.UnexpectedException

class EnrolmentEventValidatorException(message: String = "Saving EnrolmentEvent failed validation"): UnexpectedException(message)
