package com.simprints.infra.enrolment.records.repository.commcare

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.enrolment.records.repository.commcare.models.CommCareCaseSyncInfo
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache for tracking CommCare case sync information using SecureSharedPreferences.
 * Stores caseId, subjectId, and last_modified for synced CommCare cases.
 */
@SuppressLint("ApplySharedPref")
@Singleton
internal class CommCareCaseSyncCache @Inject constructor(
    securityManager: SecurityManager,
    private val jsonHelper: JsonHelper,
    private val timeHelper: TimeHelper,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val sharedPrefs = securityManager.buildEncryptedSharedPreferences(FILENAME_COMMCARE_CASE_SYNC)

    /**
     * Save or update sync information for a CommCare case
     * @param caseId The CommCare case identifier
     * @param subjectId The Simprints subject ID
     */
    suspend fun saveCaseSyncInfo(caseId: String, subjectId: String): Unit = withContext(dispatcher) {
        try {
            val currentCases = getAllCaseSyncInfo().toMutableMap()
            currentCases[caseId] = CommCareCaseSyncInfo(
                caseId = caseId,
                subjectId = subjectId,
                lastModified = timeHelper.now().ms
            )
            saveCaseSyncInfoMap(currentCases)
        } catch (e: Exception) {
            Simber.e("Failed to save CommCare case sync info for caseId: $caseId", e, tag = SYNC)
        }
    }

    /**
     * Update the last_modified timestamp for an existing case
     * @param caseId The CommCare case identifier
     */
    suspend fun updateCaseLastModified(caseId: String): Unit = withContext(dispatcher) {
        try {
            val currentCases = getAllCaseSyncInfo().toMutableMap()
            currentCases[caseId]?.let { existingCase ->
                currentCases[caseId] = existingCase.copy(lastModified = timeHelper.now().ms)
                saveCaseSyncInfoMap(currentCases)
            } ?: run {
                Simber.w("Attempted to update last_modified for non-existent caseId: $caseId", tag = SYNC)
            }
        } catch (e: Exception) {
            Simber.e("Failed to update last_modified for caseId: $caseId", e, tag = SYNC)
        }
    }

    /**
     * Delete sync information for a CommCare case (when case is no longer present in CommCare)
     * @param caseId The CommCare case identifier to delete
     */
    suspend fun deleteCaseSyncInfo(caseId: String): Unit = withContext(dispatcher) {
        try {
            val currentCases = getAllCaseSyncInfo().toMutableMap()
            if (currentCases.remove(caseId) != null) {
                saveCaseSyncInfoMap(currentCases)
            }
        } catch (e: Exception) {
            Simber.e("Failed to delete CommCare case sync info for caseId: $caseId", e, tag = SYNC)
        }
    }

    /**
     * Get sync information for a specific case
     * @param caseId The CommCare case identifier
     * @return CommCareCaseSyncInfo if found, null otherwise
     */
    suspend fun getCaseSyncInfo(caseId: String): CommCareCaseSyncInfo? = withContext(dispatcher) {
        try {
            getAllCaseSyncInfo()[caseId]
        } catch (e: Exception) {
            Simber.e("Failed to get CommCare case sync info for caseId: $caseId", e, tag = SYNC)
            null
        }
    }

    /**
     * Get all stored case sync information
     * @return Map of caseId to CommCareCaseSyncInfo
     */
    suspend fun getAllCaseSyncInfo(): Map<String, CommCareCaseSyncInfo> = withContext(dispatcher) {
        try {
            val jsonString = sharedPrefs.getString(CASE_SYNC_INFO_KEY, null)
            if (jsonString.isNullOrEmpty()) {
                emptyMap()
            } else {
                jsonHelper.fromJson(
                    json = jsonString,
                    type = object : TypeReference<Map<String, CommCareCaseSyncInfo>>() {}
                )
            }
        } catch (e: Exception) {
            Simber.e("Failed to load CommCare case sync info", e, tag = SYNC)
            emptyMap()
        }
    }

    /**
     * Clear all stored case sync information
     */
    suspend fun clearAllCaseSyncInfo(): Unit = withContext(dispatcher) {
        try {
            sharedPrefs.edit(commit = true) {
                remove(CASE_SYNC_INFO_KEY)
            }
        } catch (e: Exception) {
            Simber.e("Failed to clear CommCare case sync info", e, tag = SYNC)
        }
    }

    private suspend fun saveCaseSyncInfoMap(cases: Map<String, CommCareCaseSyncInfo>) {
        try {
            val jsonString = jsonHelper.toJson(cases)
            sharedPrefs.edit(commit = true) {
                putString(CASE_SYNC_INFO_KEY, jsonString)
            }
        } catch (e: Exception) {
            Simber.e("Failed to save CommCare case sync info map", e, tag = SYNC)
        }
    }

    companion object {
        @VisibleForTesting
        const val FILENAME_COMMCARE_CASE_SYNC = "COMMCARE_CASE_SYNC_CACHE"
        
        @VisibleForTesting
        const val CASE_SYNC_INFO_KEY = "CASE_SYNC_INFO"
    }
}