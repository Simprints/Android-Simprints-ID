package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException

class MalformedSyncOperationException(
    message: String = "People downsync operation is malformed as input for downloader worker"
): UnexpectedException(message)
