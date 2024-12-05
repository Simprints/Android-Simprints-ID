package com.simprints.infra.events.exceptions.validator

import com.simprints.infra.events.exceptions.SessionDataSourceException

internal class PersonCreationEventException(message: String): SessionDataSourceException(message)
