package com.simprints.id.data.db.sync

import com.simprints.id.services.sync.SyncTaskParameters
import okhttp3.HttpUrl
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.zip.GZIPInputStream

class NaiveSyncConnectorImp(syncTask: SyncTaskParameters) : NaiveSyncConnector {

    companion object {
        private const val PARAM_KEY = "key"
        private const val PARAM_PROJECT_KEY = "projectId"
        private const val PARAM_BATCH_SIZE = "batchSize"
        private const val PARAM_MODULE_ID = "moduleId"
        private const val PARAM_USER_ID = "userId"

        private const val URL = "sync-manager-dot-simprints-dev.appspot.com"
        private const val ENDPOINT_DOWNLOAD = "patients-sync"
        private const val REQUEST_BATCH_SIZE = "5000"
        private const val CONNECTION_TIMEOUT = 40 * 1000
    }

    private val urlForDownload by lazy {
        val builder = HttpUrl.Builder()
            .scheme("https")
            .host(URL)
            .addPathSegment(ENDPOINT_DOWNLOAD)
            .addQueryParameter(PARAM_KEY, "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0")
            .addQueryParameter(PARAM_PROJECT_KEY, "testProjectWith100kPatients" /*syncTask.projectId*/)
            .addQueryParameter(PARAM_BATCH_SIZE, REQUEST_BATCH_SIZE)

//        when (syncTask) {
//            is UserSyncTaskParameters -> builder.addQueryParameter(PARAM_USER_ID, syncTask.userId)
//            is ModuleIdSyncTaskParameters -> builder.addQueryParameter(PARAM_MODULE_ID, syncTask.moduleId)
//        }

        builder.build().url()
    }

    override val inputStreamForDownload: InputStream by lazy {
        val conn: HttpURLConnection = urlForDownload.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate")
        conn.connectTimeout = CONNECTION_TIMEOUT
        conn.readTimeout = CONNECTION_TIMEOUT
        val stream = if ("gzip" == conn.contentEncoding) {
            GZIPInputStream(conn.inputStream)
        } else {
            conn.inputStream
        }
        stream
    }
}
