package com.simprints.infra.enrolment.records.repository.domain.models

import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.tools.time.Timestamp

data class CandidateRecordBatch(
    val identities: List<CandidateRecord>,
    val loadingStartTime: Timestamp,
    val loadingEndTime: Timestamp,
)
