package com.simprints.id.exceptions.unexpected

import com.simprints.id.exceptions.UnexpectedException

class MalformedDownSyncOperationException(
    message: String = "People downsync operation is malformed as input for downloader worker"
): UnexpectedException(message)
