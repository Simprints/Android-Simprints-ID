package com.simprints.eventsystem.exceptions.validator

import com.simprints.eventsystem.exceptions.SessionDataSourceException

internal class DuplicateGuidSelectEventValidatorException(
    message: String
): SessionDataSourceException(message)
