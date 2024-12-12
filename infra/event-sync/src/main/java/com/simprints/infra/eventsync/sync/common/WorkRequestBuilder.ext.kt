package com.simprints.infra.eventsync.sync.common

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.END_SYNC_REPORTER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.START_SYNC_REPORTER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.UPLOADER
import java.util.Date

internal const val TAG_SUBJECTS_SYNC_ALL_WORKERS = "TAG_SUBJECTS_SYNC_ALL_WORKERS"
internal const val TAG_MASTER_SYNC_ID = "TAG_MASTER_SYNC_ID_"
internal const val TAG_SCHEDULED_AT = "TAG_SCHEDULED_AT_"

internal const val TAG_DOWN_MASTER_SYNC_ID = "TAG_DOWN_MASTER_SYNC_ID_"
internal const val TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS = "DOWN_${TAG_SUBJECTS_SYNC_ALL_WORKERS}"

internal const val TAG_UP_MASTER_SYNC_ID = "TAG_UP_MASTER_SYNC_ID"
internal const val TAG_SUBJECTS_UP_SYNC_ALL_WORKERS = "UP_${TAG_SUBJECTS_SYNC_ALL_WORKERS}"

internal const val MASTER_SYNC_SCHEDULERS = "TAG_MASTER_SYNC_SCHEDULERS"
internal const val MASTER_SYNC_SCHEDULER_ONE_TIME = "TAG_MASTER_SYNC_SCHEDULER_ONE_TIME"
internal const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "TAG_MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

/*
 * Add tags
 */

// Common tags
internal fun WorkRequest.Builder<*, *>.addTagForMasterSyncId(uniqueMasterSyncId: String?): WorkRequest.Builder<*, *> =
    uniqueMasterSyncId?.let { this.addTag("${TAG_MASTER_SYNC_ID}$uniqueMasterSyncId") } ?: this

internal fun WorkRequest.Builder<*, *>.addTagForScheduledAtNow(): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_SCHEDULED_AT}${Date().time}")

internal fun WorkRequest.Builder<*, *>.addCommonTagForAllSyncWorkers(): WorkRequest.Builder<*, *> =
    this.addTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)

// Down Sync Workers tags
internal fun WorkRequest.Builder<*, *>.addTagForDownSyncId(uniqueDownMasterSyncId: String): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_DOWN_MASTER_SYNC_ID}$uniqueDownMasterSyncId")

internal fun WorkRequest.Builder<*, *>.addCommonTagForDownWorkers(): WorkRequest.Builder<*, *> =
    this.addTag(TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS)

internal fun WorkRequest.Builder<*, *>.addCommonTagForDownloaders(): WorkRequest.Builder<*, *> = this.addTag(tagForType(DOWNLOADER))

// Up Sync Workers tags
internal fun WorkRequest.Builder<*, *>.addTagFoUpSyncId(uniqueDownMasterSyncId: String): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_UP_MASTER_SYNC_ID}$uniqueDownMasterSyncId")

internal fun WorkRequest.Builder<*, *>.addCommonTagForUpWorkers(): WorkRequest.Builder<*, *> = this.addTag(TAG_SUBJECTS_UP_SYNC_ALL_WORKERS)

internal fun WorkRequest.Builder<*, *>.addCommonTagForUploaders(): WorkRequest.Builder<*, *> = this.addTag(tagForType(UPLOADER))

// Last Sync Reporter Worker tags
internal fun WorkRequest.Builder<*, *>.addTagForEndSyncReporter(): WorkRequest.Builder<*, *> = this.addTag(tagForType(END_SYNC_REPORTER))

internal fun WorkRequest.Builder<*, *>.addTagForStartSyncReporter(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(START_SYNC_REPORTER))

/*
 * Use tags
 */
internal fun getUniqueSyncIdTag(syncId: String) = "$TAG_MASTER_SYNC_ID$syncId"

internal fun WorkInfo.getUniqueSyncId() = tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }?.removePrefix(TAG_MASTER_SYNC_ID)

internal fun List<WorkInfo>.filterByTags(vararg tagsToFilter: String) = this
    .filter {
        it.tags.firstOrNull { tag ->
            tagsToFilter.contains(tag)
        } != null
    }.sortedBy { it ->
        it.tags.first { it.contains(TAG_SCHEDULED_AT) }
    }

internal fun WorkManager.getAllSubjectsSyncWorkersInfo() = getWorkInfosByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)

internal fun MutableList<WorkInfo>.sortByScheduledTime() = sortBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }

internal fun List<WorkInfo>.sortByScheduledTime() = sortedBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }
