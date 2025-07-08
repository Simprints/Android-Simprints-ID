package com.simprints.infra.eventsync.sync.down

import androidx.work.Constraints
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.down.workers.CommCareEventSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker
import javax.inject.Inject

internal class CommCareEventSyncWorkersBuilder @Inject constructor(
    downSyncScopeRepository: EventDownSyncScopeRepository,
    jsonHelper: JsonHelper,
    configManager: ConfigManager,
) : BaseEventDownSyncWorkersBuilder(
    downSyncScopeRepository,
    jsonHelper,
    configManager,
) {
    override fun getWorkerClass(): Class<out BaseEventDownSyncDownloaderWorker> =
        CommCareEventSyncDownloaderWorker::class.java

    override fun getDownSyncWorkerConstraints() = Constraints
        .Builder()
        .build()
}
