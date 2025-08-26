package com.simprints.infra.sync

import androidx.core.content.edit
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject

class ImageSyncTimestampProvider @Inject constructor(
    securityManager: SecurityManager,
    private val timeHelper: TimeHelper,
) {
    private val securePrefs = securityManager.buildEncryptedSharedPreferences(SECURE_PREF_FILE_NAME)

    fun saveImageSyncCompletionTimestampNow() {
        securePrefs.edit { putLong(IMAGE_SYNC_COMPLETION_TIME_MILLIS, timeHelper.now().ms) }
    }

    fun getMillisSinceLastImageSync(): Long? = securePrefs
        .getLong(IMAGE_SYNC_COMPLETION_TIME_MILLIS, 0)
        .takeIf {
            securePrefs.contains(IMAGE_SYNC_COMPLETION_TIME_MILLIS)
        }?.let { lastSyncTimestamp ->
            timeHelper.now().ms - lastSyncTimestamp
        }

    fun getLastImageSyncTimestamp(): Long? = securePrefs
        .getLong(IMAGE_SYNC_COMPLETION_TIME_MILLIS, 0)
        .takeIf {
            securePrefs.contains(IMAGE_SYNC_COMPLETION_TIME_MILLIS)
        }

    fun clearTimestamp() {
        securePrefs.edit { clear() }
    }

    companion object {
        private const val SECURE_PREF_FILE_NAME = "93e98bc1-5b25-4805-94f6-f55ce0400747"
        private const val IMAGE_SYNC_COMPLETION_TIME_MILLIS = "IMAGE_SYNC_COMPLETION_TIME_MILLIS"
    }
}
