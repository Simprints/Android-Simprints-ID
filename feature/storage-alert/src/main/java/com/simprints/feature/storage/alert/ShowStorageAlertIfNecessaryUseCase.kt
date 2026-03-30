package com.simprints.feature.storage.alert

import android.content.Context
import android.os.StatFs
import com.simprints.core.ExternalScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowStorageAlertIfNecessaryUseCase @Inject internal constructor(
    @param:ApplicationContext private val context: Context,
    private val configRepo: ConfigRepository,
    private val showStorageNotification: ShowStorageWarningNotificationUseCase,
    @param:ExternalScope private val externalScope: CoroutineScope,
) {
    operator fun invoke() = externalScope.launch {
        val minimalRequiredSpaceMb = getMinimalRequiredSpaceMb()
        val availableStorageMb = getAvailableStorageMb()

        if (availableStorageMb < minimalRequiredSpaceMb) {
            Simber.w("Storage alert shown", StorageAlertShownException())
            showStorageNotification()
        }
    }

    private suspend fun getMinimalRequiredSpaceMb(): Int = configRepo
        .getProjectConfiguration()
        .experimental()
        .minimumFreeSpaceMb

    private fun getAvailableStorageMb(): Int = StatFs(context.filesDir.path)
        .let { (it.freeBytes * 1f) / BYTES_IN_MB }
        .toInt()
        .coerceAtLeast(0)

    companion object {
        private const val BYTES_IN_MB = 1024 * 1024
    }
}
