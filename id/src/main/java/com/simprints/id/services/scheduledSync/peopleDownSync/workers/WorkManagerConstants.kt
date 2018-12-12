package com.simprints.id.services.scheduledSync.peopleDownSync.workers

class WorkManagerConstants {
    companion object {
        const val SUBCOUNT_WORKER_TAG = "SUBCOUNT_WORKER_TAG" //Tag for each SubCountWorker
        const val SUBDOWNSYNC_WORKER_TAG = "SUBDOWNSYNC_WORKER_TAG" //Tag for each SubDownSyncWorker
        const val DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME = "DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME" //Tag for DownSyncMasterWorker
        const val DOWNSYNC_MASTER_WORKER_TAG_PERIODIC = "DOWNSYNC_MASTER_WORKER_TAG_PERIODIC" //Tag for DownSyncMasterWorker

        const val SYNC_WORKER_CHAIN = "SYNC_WORKER_CHAIN" //Tag for the chain of SubCountWorkers and SubDownSyncWorkers

        const val SYNC_WORKER_TAG = "SYNC_WORKER_TAG" //Tag for each workers involved in the down sync

    }
}
