package com.simprints.infra.enrolment.records.repository.commcare

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import javax.inject.Inject

/**
 * Service for handling CommCare sync operations with case tracking.
 * This class coordinates between CommCare data operations and the case sync cache.
 */
internal class CommCareSyncService @Inject constructor(
    private val caseSyncCache: CommCareCaseSyncCache,
) {

    /**
     * Process subject actions from CommCare sync and update the case cache accordingly.
     * @param actions List of subject actions to process
     * @param project Current project
     * @param caseIdExtractor Function to extract caseId from subject metadata
     */
    suspend fun processSubjectActionsWithCaseTracking(
        actions: List<SubjectAction>,
        project: Project,
        caseIdExtractor: (String?) -> String?,
    ) {
        actions.forEach { action ->
            when (action) {
                is SubjectAction.Creation -> {
                    val subject = action.subject
                    val caseId = caseIdExtractor(subject.metadata)
                    if (caseId != null) {
                        caseSyncCache.saveCaseSyncInfo(caseId, subject.subjectId)
                    }
                }

                is SubjectAction.Update -> {
                    // For updates, we need to update the last_modified timestamp
                    // We'll need the caseId which should be tracked somewhere
                    // This might require looking up the subject to get its metadata
                    updateCaseInfoForSubject(action.subjectId, caseIdExtractor)
                }

                is SubjectAction.Deletion -> {
                    // For deletions, we need to remove the case from cache
                    // We'll need the caseId which should be tracked somewhere
                    deleteCaseInfoForSubject(action.subjectId)
                }
            }
        }
    }

    /**
     * Update case information when a subject is updated.
     * @param subjectId The subject ID that was updated
     * @param caseIdExtractor Function to extract caseId - this might need subject lookup
     */
    private suspend fun updateCaseInfoForSubject(
        subjectId: String,
        caseIdExtractor: (String?) -> String?,
    ) {
        // Find the caseId for this subjectId from the cache
        val allCases = caseSyncCache.getAllCaseSyncInfo()
        val caseId = allCases.values.find { it.subjectId == subjectId }?.caseId
        
        if (caseId != null) {
            caseSyncCache.updateCaseLastModified(caseId)
        }
    }

    /**
     * Delete case information when a subject is deleted.
     * @param subjectId The subject ID that was deleted
     */
    private suspend fun deleteCaseInfoForSubject(subjectId: String) {
        // Find the caseId for this subjectId from the cache
        val allCases = caseSyncCache.getAllCaseSyncInfo()
        val caseId = allCases.values.find { it.subjectId == subjectId }?.caseId
        
        if (caseId != null) {
            caseSyncCache.deleteCaseSyncInfo(caseId)
        }
    }

    /**
     * Sync cases that are no longer present in CommCare by deleting their cache entries.
     * @param currentCommCareCaseIds List of case IDs currently present in CommCare
     */
    suspend fun syncDeletedCases(currentCommCareCaseIds: Set<String>) {
        val allCachedCases = caseSyncCache.getAllCaseSyncInfo()
        val casesToDelete = allCachedCases.keys - currentCommCareCaseIds
        
        casesToDelete.forEach { caseId ->
            caseSyncCache.deleteCaseSyncInfo(caseId)
        }
    }
}