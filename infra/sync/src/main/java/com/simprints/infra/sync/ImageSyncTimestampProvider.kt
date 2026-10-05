package com.simprints.infra.sync

import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.domain.sync.SyncOutcome
import com.simprints.core.domain.sync.toSyncFailureReason
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * When sample upload was last attempted, and how it turned out.
 */
@Singleton
class ImageSyncTimestampProvider @Inject constructor(
    private val securityManager: SecurityManager,
    private val timeHelper: TimeHelper,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val securePrefs by lazy { securityManager.buildEncryptedSharedPreferences(SECURE_PREF_FILE_NAME) }
    private val lastSyncOutcome = MutableStateFlow<SyncOutcome?>(null)

    /** The last attempt and how it turned out, then every later one. */
    // compareAndSet, not assignment: the worker can write between this read and the store, and the
    // stale value would otherwise win and stick until the next upload.
    fun observeLastSyncOutcome(): Flow<SyncOutcome?> =
        lastSyncOutcome.onStart { lastSyncOutcome.compareAndSet(null, getLastSyncOutcome()) }

    /** When the upload worker last ran and how it turned out; null when it never has. */
    suspend fun getLastSyncOutcome(): SyncOutcome? = withContext(dispatcher) {
        val lastAttempt = securePrefs
            .getLong(IMAGE_SYNC_ATTEMPT_TIME_MILLIS, 0)
            .takeIf { securePrefs.contains(IMAGE_SYNC_ATTEMPT_TIME_MILLIS) }
            ?.let { at ->
                SyncOutcome(
                    timestamp = Timestamp(at),
                    failure = securePrefs.getString(IMAGE_SYNC_FAILURE_REASON, null)?.toSyncFailureReason(),
                )
            }
        lastAttempt ?: readCompletionTimestamp()?.let { SyncOutcome(timestamp = Timestamp(it), failure = null) }
    }

    /**
     * When the last upload actually *completed*, which is what says how stale this device's
     * samples are. The last attempt cannot answer it - it moves even when nothing uploaded.
     */
    suspend fun getLastSuccessfulSyncTimestamp(): Long? = withContext(dispatcher) { readCompletionTimestamp() }

    private fun readCompletionTimestamp(): Long? = securePrefs
        .getLong(IMAGE_SYNC_COMPLETION_TIME_MILLIS, 0)
        .takeIf { securePrefs.contains(IMAGE_SYNC_COMPLETION_TIME_MILLIS) }

    suspend fun getMillisSinceLastImageSync(): Long? = getLastSuccessfulSyncTimestamp()?.let { timeHelper.now().ms - it }

    /** Records an attempt: when it ended, and why it failed when it did. */
    suspend fun saveImageSyncOutcomeNow(failure: SyncFailureReason?) {
        val at = timeHelper.now()
        withContext(dispatcher) {
            securePrefs.edit {
                putLong(IMAGE_SYNC_ATTEMPT_TIME_MILLIS, at.ms)
                if (failure == null) {
                    remove(IMAGE_SYNC_FAILURE_REASON)
                    putLong(IMAGE_SYNC_COMPLETION_TIME_MILLIS, at.ms)
                } else {
                    putString(IMAGE_SYNC_FAILURE_REASON, failure.name)
                }
            }
        }
        lastSyncOutcome.value = SyncOutcome(at, failure)
    }

    suspend fun clearLastSyncOutcome() {
        withContext(dispatcher) { securePrefs.edit { clear() } }
        lastSyncOutcome.value = null
    }

    companion object {
        private const val SECURE_PREF_FILE_NAME = "93e98bc1-5b25-4805-94f6-f55ce0400747"

        // The completion time keeps the key it has always been stored under, so an upgrading
        // device carries it over. The attempt keys are new and start empty on upgrade.
        private const val IMAGE_SYNC_COMPLETION_TIME_MILLIS = "IMAGE_SYNC_COMPLETION_TIME_MILLIS"
        private const val IMAGE_SYNC_ATTEMPT_TIME_MILLIS = "IMAGE_SYNC_ATTEMPT_TIME_MILLIS"
        private const val IMAGE_SYNC_FAILURE_REASON = "IMAGE_SYNC_FAILURE_REASON"
    }
}
