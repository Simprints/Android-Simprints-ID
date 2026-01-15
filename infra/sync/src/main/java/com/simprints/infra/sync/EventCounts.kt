package com.simprints.infra.sync

data class EventCounts(
    val download: Int,
    val isDownloadLowerBound: Boolean,
    val upload: Int,
    val uploadEnrolmentV2: Int,
    val uploadEnrolmentV4: Int,
)

