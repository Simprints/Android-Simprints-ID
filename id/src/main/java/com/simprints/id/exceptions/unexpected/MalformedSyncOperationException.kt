package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException

class MalformedSyncOperationException(
    message: String = "People sync operation is malformed as input for worker"
): UnexpectedException(message)
