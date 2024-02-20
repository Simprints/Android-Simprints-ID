package com.simprints.id

import android.content.Context
import androidx.work.WorkManager
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("There is no complex business logic to test")
class CleanupDeprecatedWorkersUseCase @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {

    private val wm = WorkManager.getInstance(ctx)

    /**
     * Removes deprecated workers from the work manager.
     *
     * If there is a change that could cause worker to crash
     * its tag or name should change with the old one added to respective list.
     */
    operator fun invoke() {
        namesForDeprecatedWorkers().forEach { wm.cancelUniqueWork(it) }
        tagsForDeprecatedWorkers().forEach { wm.cancelAllWorkByTag(it) }
    }

    private fun namesForDeprecatedWorkers() = listOf(
        "remote-config-work", // 2022.4.0
        "security-status-check-work", // 2023.2.0
        "image-upsync-work", // 2023.2.0
        "project-configuration-work", // 2024.1.1
        "security-status-check-work-v2", // 2024.1.1
        "security-status-check-work-one-time-v2", // 2024.1.1
        "project-sync-work", // 2024.1.1
        "device-sync-work", // 2024.1.1
    )

    private fun tagsForDeprecatedWorkers() = listOf(
        "TAG_PEOPLE_SYNC_WORKER_TYPE_DOWN_COUNTER", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_DOWNLOADER", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_END_SYNC_REPORTER", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_START_SYNC_REPORTER", // 2023.1.0
        "MASTER_SYNC_SCHEDULERS", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_UP_COUNTER", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_UPLOADER", // 2023.1.0
    )

}
