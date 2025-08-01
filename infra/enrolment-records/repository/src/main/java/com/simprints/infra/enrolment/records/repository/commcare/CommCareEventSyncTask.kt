package com.simprints.infra.enrolment.records.repository.commcare

import android.content.Context
import androidx.core.net.toUri
import com.simprints.core.DispatcherBG
import com.simprints.infra.enrolment.records.repository.commcare.domain.CommCareCase
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Task for syncing CommCare case data and tracking case metadata.
 * Queries CommCare's content provider to extract case information and maintains
 * sync tracking records with case IDs, subject IDs, and last modified timestamps.
 */
class CommCareEventSyncTask @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commCareCaseRepository: CommCareCaseRepository,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) {

    /**
     * Sync CommCare cases from a specific package.
     * Extracts caseId, subjectId (from simprintsId field), and last_modified timestamp.
     * 
     * @param callerPackageName The package name of the CommCare app
     * @return List of synced cases
     */
    suspend fun syncCommCareCases(callerPackageName: String): List<CommCareCase> = withContext(dispatcher) {
        val syncedCases = mutableListOf<CommCareCase>()
        
        try {
            val caseMetadataUri = getCaseMetadataUri(callerPackageName)
            
            context.contentResolver.query(
                caseMetadataUri,
                null, // Get all columns
                null,
                null,
                null,
            )?.use { cursor ->
                val caseIdColumnIndex = cursor.getColumnIndex(COLUMN_CASE_ID)
                val lastModifiedColumnIndex = cursor.getColumnIndex(COLUMN_LAST_MODIFIED)
                
                if (caseIdColumnIndex == -1) {
                    Simber.w("COLUMN_CASE_ID not found in CommCare case metadata")
                    return@withContext emptyList()
                }
                
                if (lastModifiedColumnIndex == -1) {
                    Simber.w("COLUMN_LAST_MODIFIED not found in CommCare case metadata, using current timestamp")
                }
                
                while (cursor.moveToNext()) {
                    val caseId = cursor.getString(caseIdColumnIndex)
                    val lastModified = if (lastModifiedColumnIndex != -1) {
                        cursor.getLong(lastModifiedColumnIndex)
                    } else {
                        System.currentTimeMillis()
                    }
                    
                    // Get subjectId (simprintsId) from case data
                    val subjectId = getSubjectIdFromCaseData(caseId, callerPackageName)
                    
                    if (caseId != null && subjectId != null) {
                        val commCareCase = CommCareCase(
                            caseId = caseId,
                            subjectId = subjectId,
                            lastModified = lastModified,
                        )
                        
                        // Save or update the case tracking record
                        commCareCaseRepository.saveCase(commCareCase)
                        syncedCases.add(commCareCase)
                    }
                }
            }
        } catch (e: Exception) {
            Simber.e("Error while syncing CommCare cases", e)
        }
        
        syncedCases
    }

    /**
     * Update an existing case's last modified timestamp
     */
    suspend fun updateCaseLastModified(caseId: String, lastModified: Long) {
        val existingCase = commCareCaseRepository.getCase(caseId)
        if (existingCase != null) {
            val updatedCase = existingCase.copy(lastModified = lastModified)
            commCareCaseRepository.saveCase(updatedCase)
        }
    }

    /**
     * Delete case tracking record when a subject is deleted
     */
    suspend fun deleteCaseBySubjectId(subjectId: String) {
        commCareCaseRepository.deleteCaseBySubjectId(subjectId)
    }

    /**
     * Get subject ID (simprintsId field) from CommCare case data
     */
    private fun getSubjectIdFromCaseData(caseId: String, callerPackageName: String): String? {
        val caseDataUri = getCaseDataUri(callerPackageName).buildUpon().appendPath(caseId).build()
        
        return try {
            context.contentResolver.query(caseDataUri, null, null, null, null)?.use { cursor ->
                val datumIdColumnIndex = cursor.getColumnIndex(COLUMN_DATUM_ID)
                val valueColumnIndex = cursor.getColumnIndex(COLUMN_VALUE)
                
                if (datumIdColumnIndex == -1 || valueColumnIndex == -1) {
                    return@use null
                }
                
                while (cursor.moveToNext()) {
                    val datumId = cursor.getString(datumIdColumnIndex)
                    if (datumId == SIMPRINTS_ID_FIELD) {
                        return@use cursor.getString(valueColumnIndex)
                    }
                }
                null
            }
        } catch (e: Exception) {
            Simber.e("Error getting subjectId for case $caseId", e)
            null
        }
    }

    private fun getCaseMetadataUri(packageName: String) = "content://$packageName.case/casedb/case".toUri()
    
    private fun getCaseDataUri(packageName: String) = "content://$packageName.case/casedb/data".toUri()

    companion object {
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_LAST_MODIFIED = "last_modified"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"
        const val SIMPRINTS_ID_FIELD = "simprintsId"
    }
}