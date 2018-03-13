package com.simprints.id.data.db.sync

import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.services.sync.SyncTaskParameters.ModuleIdSyncTaskParameters
import com.simprints.id.services.sync.SyncTaskParameters.UserSyncTaskParameters
import okhttp3.HttpUrl
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.GZIPInputStream

class NaiveSyncConnectorImpl(firestoreToken: String): NaiveSyncConnector {

    companion object {
        private const val PARAM_KEY = "key"
        private const val PARAM_PROJECT_KEY = "projectId"
        private const val PARAM_BATCH_SIZE = "batchSize"
        private const val PARAM_MODULE_ID = "moduleId"
        private const val PARAM_USER_ID = "userId"
        private const val PARAM_PATIENT_ID = "patientId"
        private const val PARAM_SYNC_TIME = "syncTime"

        private const val URL = "sync-manager-dot-simprints-dev.appspot.com"
        private const val ENDPOINT_DOWNLOAD = "patients-sync"
        private const val REQUEST_BATCH_SIZE = "5000"
        private const val CONNECTION_TIMEOUT = 40 * 1000
    }

    override fun getInputStreamForDownSyncingRequest(syncTask: SyncTaskParameters, syncTime: Date): InputStream {
        val conn: HttpURLConnection = getUrlToDownSync(syncTask, syncTime).openConnection() as HttpURLConnection
        return openStream(conn)
    }

    override fun getInputStreamForDownloadingPatientRequest(patientId: String): InputStream {
        val conn: HttpURLConnection = getUrlToDownloadSinglePatient(patientId).openConnection() as HttpURLConnection
        return openStream(conn)
    }

    private fun openStream(conn: HttpURLConnection): InputStream {
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate")
        conn.connectTimeout = CONNECTION_TIMEOUT
        conn.readTimeout = CONNECTION_TIMEOUT
        return if ("gzip" == conn.contentEncoding) {
            GZIPInputStream(conn.inputStream)
        } else {
            conn.inputStream
        }
    }

    private fun getUrlToDownloadSinglePatient(patientId: String): URL {
        val builder = getBaseUrlToDownloadPatients().apply {
            addQueryParameter(PARAM_KEY, "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0")
            addQueryParameter(PARAM_PATIENT_ID, patientId)
        }
        return builder.build().url()
    }

    private fun getUrlToDownSync(syncTask: SyncTaskParameters, syncTime: Date): URL {
        val builder = getBaseUrlToDownloadPatients().apply {
            addQueryParameter(PARAM_KEY, "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0")
            addQueryParameter(PARAM_PROJECT_KEY, syncTask.projectId)
            addQueryParameter(PARAM_BATCH_SIZE, REQUEST_BATCH_SIZE)
            addQueryParameter(PARAM_SYNC_TIME, syncTime.toString())

            when (syncTask) {
                is UserSyncTaskParameters -> addQueryParameter(PARAM_USER_ID, syncTask.userId)
                is ModuleIdSyncTaskParameters -> addQueryParameter(PARAM_MODULE_ID, syncTask.moduleId)
            }
        }
        return builder.build().url()
    }

    private fun getBaseUrlToDownloadPatients(): HttpUrl.Builder {
        return HttpUrl.Builder()
            .scheme("https")
            .host(URL)
            .addPathSegment(ENDPOINT_DOWNLOAD)

    }
}
