package com.simprints.infra.sync

data class SyncableCounts(
    val eventsToDownload: Int,
    val isEventsToDownloadLowerBound: Boolean,
    val eventsToUpload: Int,
    val eventsToUploadEnrolmentV2: Int,
    val eventsToUploadEnrolmentV4: Int,
    val imagesToUpload: Int,
)

