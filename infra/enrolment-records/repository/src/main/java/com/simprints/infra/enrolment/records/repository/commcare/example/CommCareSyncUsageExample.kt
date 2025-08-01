package com.simprints.infra.enrolment.records.repository.commcare.example

import com.simprints.infra.enrolment.records.repository.commcare.CommCareEventSyncTask
import javax.inject.Inject

/**
 * Example usage of CommCare case sync functionality.
 * This shows how to integrate the CommCareEventSyncTask with existing sync workflows.
 */
class CommCareSyncUsageExample @Inject constructor(
    private val commCareEventSyncTask: CommCareEventSyncTask,
) {

    /**
     * Example: Sync CommCare cases and track the results
     */
    suspend fun syncCommCareCases(commCarePackageName: String) {
        try {
            // Sync cases from CommCare and save tracking records
            val syncedCases = commCareEventSyncTask.syncCommCareCases(commCarePackageName)
            
            println("Successfully synced ${syncedCases.size} CommCare cases:")
            syncedCases.forEach { case ->
                println("  - Case: ${case.caseId}, Subject: ${case.subjectId}, Modified: ${case.lastModified}")
            }
        } catch (e: Exception) {
            println("Failed to sync CommCare cases: ${e.message}")
        }
    }

    /**
     * Example: Update a case when you detect changes
     */
    suspend fun updateCaseModification(caseId: String, newTimestamp: Long) {
        try {
            commCareEventSyncTask.updateCaseLastModified(caseId, newTimestamp)
            println("Updated case $caseId with timestamp $newTimestamp")
        } catch (e: Exception) {
            println("Failed to update case: ${e.message}")
        }
    }

    /**
     * Example: Clean up case tracking when subject is deleted
     * Note: This is automatically called by RoomEnrolmentRecordLocalDataSource.deleteSubject()
     * but can be called manually if needed
     */
    suspend fun cleanupDeletedSubject(subjectId: String) {
        try {
            commCareEventSyncTask.deleteCaseBySubjectId(subjectId)
            println("Cleaned up CommCare case tracking for subject: $subjectId")
        } catch (e: Exception) {
            println("Failed to cleanup case tracking: ${e.message}")
        }
    }
}