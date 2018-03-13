package com.simprints.id.data.db.sync

import com.simprints.id.services.sync.SyncTaskParameters
import java.io.InputStream
import java.util.*

interface NaiveSyncConnector {

    fun getInputStreamForDownSyncingRequest(syncTask: SyncTaskParameters, lastSyncTime: Date): InputStream
    fun getInputStreamForDownloadingPatientRequest(patientId: String): InputStream
}
