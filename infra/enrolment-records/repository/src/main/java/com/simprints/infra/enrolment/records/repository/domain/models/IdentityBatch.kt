package com.simprints.infra.enrolment.records.repository.domain.models

import com.simprints.core.tools.time.Timestamp

data class IdentityBatch<T>(
    val identities: List<T>,
    val loadingStartTime: Timestamp,
    val loadingEndTime: Timestamp,
)
