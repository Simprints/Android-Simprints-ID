package com.simprints.infra.enrolment.records.repository.domain.models

import com.simprints.core.domain.sample.Identity
import com.simprints.core.tools.time.Timestamp

data class IdentityBatch(
    val identities: List<Identity>,
    val loadingStartTime: Timestamp,
    val loadingEndTime: Timestamp,
)
