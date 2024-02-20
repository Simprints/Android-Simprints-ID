package com.simprints.infra.sync

import java.util.concurrent.TimeUnit

internal object SyncConstants {

    val SYNC_REPEAT_UNIT = TimeUnit.MINUTES

    const val PROJECT_SYNC_WORK_NAME = "project-sync-work-v2"
    const val PROJECT_SYNC_REPEAT_INTERVAL = BuildConfig.PROJECT_PERIODIC_WORKER_INTERVAL_MINUTES

    const val DEVICE_SYNC_WORK_NAME = "device-sync-work-v2"
    const val DEVICE_SYNC_WORK_NAME_ONE_TIME = "device-sync-work-one-time"
    const val DEVICE_SYNC_REPEAT_INTERVAL = BuildConfig.DEVICE_PERIODIC_WORKER_INTERVAL_MINUTES

    const val IMAGE_UP_SYNC_WORK_NAME = "image-upsync-work-v3"
    const val IMAGE_UP_SYNC_REPEAT_INTERVAL = BuildConfig.IMAGE_PERIODIC_WORKER_INTERVAL_MINUTES

    const val RECORD_UPLOAD_WORK_NAME = "upload-enrolment-record-work-one-time"
    const val RECORD_UPLOAD_INPUT_ID_NAME = "INPUT_ID_NAME"
    const val RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME = "INPUT_SUBJECT_IDS_NAME"
}
