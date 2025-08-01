package com.simprints.infra.enrolment.records.repository.commcare

import com.simprints.infra.enrolment.records.repository.commcare.domain.CommCareCase

/**
 * Repository interface for managing CommCare case sync tracking.
 * Handles storage and retrieval of case-to-subject mappings with last modified timestamps.
 */
interface CommCareCaseRepository {
    /**
     * Save or update a CommCare case sync record
     */
    suspend fun saveCase(case: CommCareCase)

    /**
     * Save or update multiple CommCare case sync records
     */
    suspend fun saveCases(cases: List<CommCareCase>)

    /**
     * Get a case by its caseId
     */
    suspend fun getCase(caseId: String): CommCareCase?

    /**
     * Get a case by its subjectId
     */
    suspend fun getCaseBySubjectId(subjectId: String): CommCareCase?

    /**
     * Get all synced cases
     */
    suspend fun getAllCases(): List<CommCareCase>

    /**
     * Delete a case by caseId
     */
    suspend fun deleteCase(caseId: String)

    /**
     * Delete a case by subjectId (when subject is deleted)
     */
    suspend fun deleteCaseBySubjectId(subjectId: String)

    /**
     * Delete all case records
     */
    suspend fun deleteAllCases()
}