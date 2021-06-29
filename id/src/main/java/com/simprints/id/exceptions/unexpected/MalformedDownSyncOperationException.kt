package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException

class MalformedDownSyncOperationException(
    message: String = "People downsync operation is malformed as input for downloader worker"
): UnexpectedException(message)
