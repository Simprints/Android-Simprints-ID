package com.simprints.infra.matching

import com.simprints.core.tools.time.Timestamp

data class MatchBatchInfo(
    val loadingStartTime: Timestamp,
    val loadingEndTime: Timestamp,
    val comparingStartTime: Timestamp,
    val comparingEndTime: Timestamp,
    val count: Int,
)
