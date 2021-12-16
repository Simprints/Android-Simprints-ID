package com.simprints.eventsystem.exceptions.validator

import com.simprints.eventsystem.exceptions.SessionDataSourceException

class GuidSelectEventValidatorException(message: String): SessionDataSourceException(message)
class DuplicateGuidSelectEventValidatorException(message: String): SessionDataSourceException(message)
