package com.simprints.infra.eventsync.sync.common

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.*
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import java.util.*

const val TAG_SUBJECTS_SYNC_ALL_WORKERS = "TAG_SUBJECTS_SYNC_ALL_WORKERS"
const val TAG_MASTER_SYNC_ID = "TAG_MASTER_SYNC_ID_"
const val TAG_SCHEDULED_AT = "TAG_SCHEDULED_AT_"

const val TAG_DOWN_MASTER_SYNC_ID = "TAG_DOWN_MASTER_SYNC_ID_"
const val TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS = "DOWN_${TAG_SUBJECTS_SYNC_ALL_WORKERS}"

const val TAG_UP_MASTER_SYNC_ID = "TAG_UP_MASTER_SYNC_ID"
const val TAG_SUBJECTS_UP_SYNC_ALL_WORKERS = "UP_${TAG_SUBJECTS_SYNC_ALL_WORKERS}"

const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"


/**
 * Add tags
 */
// Common tags
fun WorkRequest.Builder<*, *>.addTagForMasterSyncId(uniqueMasterSyncId: String?): WorkRequest.Builder<*, *> =
    uniqueMasterSyncId?.let { this.addTag("${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}") } ?: this

fun WorkRequest.Builder<*, *>.addTagForScheduledAtNow(): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_SCHEDULED_AT}${Date().time}")

fun WorkRequest.Builder<*, *>.addCommonTagForAllSyncWorkers(): WorkRequest.Builder<*, *> =
    this.addTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)

// Down Sync Workers tags
fun WorkRequest.Builder<*, *>.addTagForDownSyncId(uniqueDownMasterSyncId: String): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_DOWN_MASTER_SYNC_ID}${uniqueDownMasterSyncId}")

fun WorkRequest.Builder<*, *>.addCommonTagForDownWorkers(): WorkRequest.Builder<*, *> =
    this.addTag(TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS)

fun WorkRequest.Builder<*, *>.addCommonTagForDownloaders(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(DOWNLOADER))

fun WorkRequest.Builder<*, *>.addCommonTagForDownCounters(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(DOWN_COUNTER))

// Up Sync Workers tags
fun WorkRequest.Builder<*, *>.addTagFoUpSyncId(uniqueDownMasterSyncId: String): WorkRequest.Builder<*, *> =
    this.addTag("${TAG_UP_MASTER_SYNC_ID}${uniqueDownMasterSyncId}")

fun WorkRequest.Builder<*, *>.addCommonTagForUpWorkers(): WorkRequest.Builder<*, *> =
    this.addTag(TAG_SUBJECTS_UP_SYNC_ALL_WORKERS)

fun WorkRequest.Builder<*, *>.addCommonTagForUploaders(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(UPLOADER))

fun WorkRequest.Builder<*, *>.addCommonTagForUpCounters(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(UP_COUNTER))

// Last Sync Reporter Worker tags
fun WorkRequest.Builder<*, *>.addTagForEndSyncReporter(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(END_SYNC_REPORTER))

fun WorkRequest.Builder<*, *>.addTagForStartSyncReporter(): WorkRequest.Builder<*, *> =
    this.addTag(tagForType(START_SYNC_REPORTER))

// Master Worker tags
fun WorkRequest.Builder<*, *>.addTagForSyncMasterWorkers(): WorkRequest.Builder<*, *> = this.addTag(MASTER_SYNC_SCHEDULERS)

fun WorkRequest.Builder<*, *>.addTagForOneTimeSyncMasterWorker(): WorkRequest.Builder<*, *> = this.addTag(MASTER_SYNC_SCHEDULER_ONE_TIME)
fun WorkRequest.Builder<*, *>.addTagForBackgroundSyncMasterWorker(): WorkRequest.Builder<*, *> = this.addTag(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)

/**
 * Use tags
 */
fun getUniqueSyncIdTag(syncId: String) = "$TAG_MASTER_SYNC_ID$syncId"

fun WorkInfo.getUniqueSyncId() = tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }?.removePrefix(TAG_MASTER_SYNC_ID)
fun List<WorkInfo>.filterByTags(vararg tagsToFilter: String) =
    this.filter {
        it.tags.firstOrNull { tag ->
            tagsToFilter.contains(tag)
        } != null
    }.sortedBy { it ->
        it.tags.first { it.contains(TAG_SCHEDULED_AT) }
    }

fun WorkManager.getAllSubjectsSyncWorkersInfo() = getWorkInfosByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)
fun WorkManager.cancelAllSubjectsSyncWorkers() = cancelAllWorkByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)
fun MutableList<WorkInfo>.sortByScheduledTime() = sortBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }
fun List<WorkInfo>.sortByScheduledTime() = sortedBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }
