package com.simprints.infra.sync

/**
 * Combined counters for syncable entities (events & images) in the current project
 */
data class SyncableCounts(
    val recordsTotal: Int,
    val eventsToDownload: Int,
    val isEventsToDownloadLowerBound: Boolean,
    val eventsToUpload: Int,
    val eventsToUploadEnrolmentV2: Int,
    val eventsToUploadEnrolmentV4: Int,
    val imagesToUpload: Int,
)
