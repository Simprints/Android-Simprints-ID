package com.simprints.infra.eventsync

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULERS
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("There is no complex logic to test")
class EventSyncWorkerTagRepository @Inject internal constructor(
    private val timeHelper: TimeHelper,
) {
    fun getPeriodicWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getOneTimeWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_ONE_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getAllWorkerTag(): String = TAG_SUBJECTS_SYNC_ALL_WORKERS
}
