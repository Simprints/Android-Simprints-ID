package com.simprints.id.exceptions.unexpected

import com.simprints.id.exceptions.UnexpectedException

class MigrationToNewEventArchitectureException(
    message: String = "Something went wrong in the migration of subjects to the events db.", cause: Throwable) : UnexpectedException(message, cause)
