package com.simprints.infra.config.sync

import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConfigSyncCache @Inject constructor(
    securityManager: SecurityManager,
    private val timeHelper: TimeHelper,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val sharedPrefs =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)

    suspend fun saveUpdateTime() = withContext(dispatcher) {
        sharedPrefs.edit { putLong(KEY_LAST_SYNC_TIME, timeHelper.now().ms) }
    }

    suspend fun sinceLastUpdateTime(): String = withContext(dispatcher) {
        sharedPrefs
            .getLong(KEY_LAST_SYNC_TIME, -1)
            .takeIf { it >= 0 }
            ?.let { timeHelper.readableBetweenNowAndTime(Timestamp(it)) }
            .orEmpty()
    }

    companion object {
        private const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_CONFIG_UPDATE_TIME"
        private const val KEY_LAST_SYNC_TIME = "KEY_LAST_SYNC_TIME"
    }
}
