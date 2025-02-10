package com.simprints.infra.sync.usecase

import androidx.work.WorkManager
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("There is no complex business logic to test")
internal class CleanupDeprecatedWorkersUseCase @Inject constructor(
    private val workManager: WorkManager,
) {
    /**
     * Removes deprecated workers from the work manager.
     *
     * If there is a change that could cause worker to crash
     * its tag or name should change with the old one added to respective list.
     */
    operator fun invoke() {
        namesForDeprecatedWorkers().forEach { workManager.cancelUniqueWork(it) }
        tagsForDeprecatedWorkers().forEach { workManager.cancelAllWorkByTag(it) }
    }

    private fun namesForDeprecatedWorkers() = listOf(
        "image-upsync-work-v3", // renamed to "file-upsync-work" in 2025.1.0
        "remote-config-work", // 2022.4.0
        "security-status-check-work", // 2023.2.0
        "image-upsync-work", // 2023.2.0
        "project-configuration-work", // 2024.1.1
        "security-status-check-work-v2", // 2024.1.1
        "security-status-check-work-one-time-v2", // 2024.1.1
        "project-sync-work", // 2024.1.1
        "device-sync-work", // 2024.1.1
        "image-upsync-work-v2", // 2024.1.1
        "firmware-file-update-work", // 2024.1.1
        "TAG_MASTER_SYNC_SCHEDULER_PERIODIC_TIME", // 2024.1.1, tag was indeed used as worker name
        "TAG_MASTER_SYNC_SCHEDULER_ONE_TIME", // 2024.1.1, tag was indeed used as worker name
    )

    private fun tagsForDeprecatedWorkers() = listOf(
        "TAG_PEOPLE_SYNC_WORKER_TYPE_DOWN_COUNTER", // 2023.1.0
        "MASTER_SYNC_SCHEDULERS", // 2023.1.0
        "TAG_PEOPLE_SYNC_WORKER_TYPE_UP_COUNTER", // 2023.1.0
    )
}
