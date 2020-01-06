package com.simprints.id.services.scheduledSync.people.master

import androidx.work.WorkInfo
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.TAG_PEOPLE_SYNC_WORKER_TYPE


enum class PeopleSyncWorkerType {
    DOWN_COUNTER,
    UP_COUNTER,
    UPLOADER,
    DOWNLOADER;

    companion object {

        const val TAG_PEOPLE_SYNC_WORKER_TYPE = "TAG_PEOPLE_SYNC_WORKER_TYPE_"

        fun tagForType(type: PeopleSyncWorkerType) = "${TAG_PEOPLE_SYNC_WORKER_TYPE}${type}"
    }
}

fun WorkInfo.extractSyncWorkerType() =
    this.tags.first { it.contains(TAG_PEOPLE_SYNC_WORKER_TYPE) }?.removePrefix(TAG_PEOPLE_SYNC_WORKER_TYPE)
