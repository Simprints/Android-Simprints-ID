package com.simprints.id.services.sync.subjects.master.models


enum class SubjectsSyncWorkerType {
    DOWN_COUNTER,
    UP_COUNTER,
    UPLOADER,
    DOWNLOADER,
    END_SYNC_REPORTER,
    START_SYNC_REPORTER;

    companion object {

        private const val TAG_PEOPLE_SYNC_WORKER_TYPE = "TAG_PEOPLE_SYNC_WORKER_TYPE_"

        fun tagForType(type: SubjectsSyncWorkerType) = "$TAG_PEOPLE_SYNC_WORKER_TYPE${type}"
    }
}
