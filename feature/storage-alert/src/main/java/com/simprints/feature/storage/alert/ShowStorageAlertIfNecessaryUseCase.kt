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
        val minimalRequiredSpacePercent = getMinimalRequiredSpacePercent()
        val availableStoragePercent = getAvailableStoragePercentage()

        if (availableStoragePercent < minimalRequiredSpacePercent) {
            Simber.w("Storage alert shown", StorageAlertShownException())
            showStorageNotification()
        }
    }

    private suspend fun getMinimalRequiredSpacePercent(): Int = configRepo
        .getProjectConfiguration()
        .experimental()
        .minimumFreeSpacePercent

    /*
     * Note that [StatFs.getTotalBytes] does not include reserved system space,
     * so the reported percent might not match the intuitive value from device settings,
     * but it is sufficient for our use case as we only care about the space that the app can use for its files.
     */
    private fun getAvailableStoragePercentage(): Int = StatFs(context.filesDir.path)
        // StatFs does not account for reserved system space, so the total space may be less than expected.
        .let { (it.freeBytes * 100f) / it.totalBytes }
        .toInt()
        .coerceIn(0, 100)
}
