package com.simprints.infra.sync

import java.util.concurrent.TimeUnit

internal object SyncConstants {
    val SYNC_TIME_UNIT = TimeUnit.MINUTES
    const val DEFAULT_BACKOFF_INTERVAL_MINUTES = 5L

    const val PROJECT_SYNC_WORK_NAME = "project-sync-work-v2"
    const val PROJECT_SYNC_WORK_NAME_ONE_TIME = "project-sync-work-v2-one-time"
    const val PROJECT_SYNC_REPEAT_INTERVAL = BuildConfig.PROJECT_DOWN_SYNC_WORKER_INTERVAL_MINUTES

    const val DEVICE_SYNC_WORK_NAME = "device-sync-work-v2"
    const val DEVICE_SYNC_WORK_NAME_ONE_TIME = "device-sync-work-one-time"
    const val DEVICE_SYNC_REPEAT_INTERVAL = BuildConfig.DEVICE_DOWN_SYNC_WORKER_INTERVAL_MINUTES

    const val FILE_UP_SYNC_WORK_NAME = "file-upsync-work"
    const val FILE_UP_SYNC_REPEAT_INTERVAL = BuildConfig.FILE_UP_SYNC_WORKER_INTERVAL_MINUTES

    const val RECORD_UPLOAD_WORK_NAME = "upload-enrolment-record-work-one-time"
    const val RECORD_UPLOAD_INPUT_ID_NAME = "INPUT_ID_NAME"
    const val RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME = "INPUT_SUBJECT_IDS_NAME"

    const val FIRMWARE_UPDATE_WORK_NAME = "firmware-file-update-work-v2"
    const val FIRMWARE_UPDATE_REPEAT_INTERVAL = BuildConfig.FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES

    const val EVENT_SYNC_WORK_NAME = "event-sync-work"
    const val EVENT_SYNC_WORK_NAME_ONE_TIME = "event-sync-work-one-time"
    const val EVENT_SYNC_WORKER_INTERVAL = BuildConfig.EVENT_SYNC_WORKER_INTERVAL_MINUTES
}
