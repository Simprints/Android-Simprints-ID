package com.simprints.id.services.scheduledSync.peopleDownSync.workers

class WorkManagerConstants {
    companion object {
        const val DOWNSYNC_WORKER_CHAIN_UNIQUE_NAME = "DOWNSYNC_WORKER_CHAIN_UNIQUE_NAME" //Tag for the chain of CountWorkers and SubDownSyncWorkers

        const val COUNT_WORKER_TAG = "COUNT_WORKER_TAG" //Tag for each CountWorker
        const val SUBDOWNSYNC_WORKER_TAG = "SUBDOWNSYNC_WORKER_TAG" //Tag for each SubDownSyncWorker
        const val DOWNSYNC_CHAIN_ONE_TIME_TAG = "DOWNSYNC_CHAIN_ONE_TIME_TAG" //Tag for DownSyncMasterWorker
        const val DOWNSYNC_CHAIN_PERIODIC_TAG = "DOWNSYNC_CHAIN_PERIODIC_TAG" //Tag for DownSyncMasterWorker

        const val SYNC_WORKER_TAG = "SYNC_WORKER_TAG" //Tag for each workers involved in the down sync

        const val RESULT = "RESULT"
    }
}
