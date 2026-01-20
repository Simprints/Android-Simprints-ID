package com.simprints.infra.sync

/**
 * Combined counters for syncable entities (events & samples) in the current project
 */
data class SyncableCounts(
    val totalRecords: Int,
    val recordEventsToDownload: Int,
    val isRecordEventsToDownloadLowerBound: Boolean,
    val eventsToUpload: Int,
    val enrolmentsToUploadV2: Int,
    val enrolmentsToUploadV4: Int,
    val samplesToUpload: Int,
)
