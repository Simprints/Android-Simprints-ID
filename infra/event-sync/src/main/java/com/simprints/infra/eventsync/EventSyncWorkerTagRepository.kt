package com.simprints.infra.eventsync

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULERS
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_UP_SYNC_ALL_WORKERS
import javax.inject.Inject

class EventSyncWorkerTagRepository @Inject internal constructor(
    private val timeHelper: TimeHelper,
) {
    fun getUpSyncPeriodicWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getUpSyncOneTimeWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_ONE_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getDownSyncPeriodicWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getDownSyncOneTimeWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_ONE_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    fun getUpSyncAllWorkersTag(): String = TAG_SUBJECTS_UP_SYNC_ALL_WORKERS

    fun getDownSyncAllWorkersTag(): String = TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS

    /** Returns a tag that matches all event sync workers (both up and down directions). */
    fun getAllWorkerTag(): String = TAG_SUBJECTS_SYNC_ALL_WORKERS

    @Deprecated("Use getUpSyncPeriodicWorkTags() or getDownSyncPeriodicWorkTags()")
    fun getPeriodicWorkTags(): List<String> = getUpSyncPeriodicWorkTags()

    @Deprecated("Use getUpSyncOneTimeWorkTags() or getDownSyncOneTimeWorkTags()")
    fun getOneTimeWorkTags(): List<String> = getUpSyncOneTimeWorkTags()
}
