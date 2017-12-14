package com.simprints.id.services.progress.client

import com.simprints.id.services.progress.service.ProgressServiceBinder
import com.simprints.id.services.progress.service.ProgressTaskParameters


interface ProgressConnection<in T: ProgressTaskParameters>: ProgressServiceBinder<T> {

    /**
     * Disconnect from the service.
     * Do not call on UI thread.
     */
    fun unbind()

}
