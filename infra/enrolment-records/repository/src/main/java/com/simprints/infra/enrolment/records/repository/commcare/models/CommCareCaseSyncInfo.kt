package com.simprints.infra.enrolment.records.repository.commcare.models

/**
 * Data class representing sync information for a CommCare case
 * @param caseId The CommCare case identifier
 * @param subjectId The Simprints subject ID (simprintsId field in CommCare)
 * @param lastModified Timestamp of when this case was last modified/synced
 */
data class CommCareCaseSyncInfo(
    val caseId: String,
    val subjectId: String,
    val lastModified: Long,
)