package com.simprints.id.services.sync.events.master.models


enum class EventSyncWorkerType {
    DOWN_COUNTER,
    UP_COUNTER,
    UPLOADER,
    DOWNLOADER,
    END_SYNC_REPORTER,
    START_SYNC_REPORTER;

    companion object {

        private const val TAG_PEOPLE_SYNC_WORKER_TYPE = "TAG_PEOPLE_SYNC_WORKER_TYPE_"

        fun tagForType(type: EventSyncWorkerType) = "$TAG_PEOPLE_SYNC_WORKER_TYPE${type}"
    }
}
