package com.simprints.infra.enrolment.records.repository.commcare.domain

/**
 * Domain model representing a CommCare case sync tracking record.
 * Contains the mapping between CommCare caseId, Simprints subjectId and last modified timestamp.
 */
data class CommCareCase(
    val caseId: String,
    val subjectId: String,
    val lastModified: Long,
)