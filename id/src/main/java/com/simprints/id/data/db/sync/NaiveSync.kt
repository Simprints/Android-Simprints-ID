package com.simprints.id.data.db.sync

import com.google.gson.stream.JsonReader
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.services.sync.SyncTaskParameters.ModuleIdSyncTaskParameters
import com.simprints.id.services.sync.SyncTaskParameters.UserSyncTaskParameters
import com.simprints.id.sync.models.RealmSyncInfo
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.Progress
import io.reactivex.Emitter
import io.realm.Realm
import io.realm.RealmConfiguration
import okhttp3.HttpUrl
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.util.zip.GZIPInputStream

class NaiveSync(private val isInterrupted: () -> Boolean,
                private val emitter: Emitter<Progress>,
                private val syncTask: SyncTaskParameters,
                private val realmConfig: RealmConfiguration) {

    companion object {
        const val PARAM_KEY = "key"
        const val PARAM_PROJECT_KEY = "projectId"
        const val PARAM_BATCH_SIZE = "batchSize"
        const val PARAM_MODULE_ID = "moduleId"
        const val PARAM_USER_ID = "userId"

        const val URL = "sync-manager-dot-simprints-dev.appspot.com"
        const val ENDPOINT_DOWNLOAD = "patients-sync"
        const val REQUEST_BATCH_SIZE = "5000"
        const val REALM_BATCH_SIZE = 10000
        const val UPDATE_UI_BATCH_SIZE = 100
        const val CONNECTION_TIMEOUT = 40 * 1000
    }

    private val urlForDownload by lazy {
        val builder = HttpUrl.Builder()
            .scheme("https")
            .host(URL)
            .addPathSegment(ENDPOINT_DOWNLOAD)
            .addQueryParameter(PARAM_KEY, "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0")
            .addQueryParameter(PARAM_PROJECT_KEY, syncTask.projectId)
            .addQueryParameter(PARAM_BATCH_SIZE, REQUEST_BATCH_SIZE)

        when (syncTask) {
            is UserSyncTaskParameters -> {
                builder.addQueryParameter(PARAM_USER_ID, syncTask.userId)
            }
            is ModuleIdSyncTaskParameters ->
                builder.addQueryParameter(PARAM_MODULE_ID, syncTask.moduleId)
        }

        builder.build().url()
    }

    fun sync() {
        downloadNewPatients()
    }

    private fun downloadNewPatients() {
        Timber.d("Url: $")
        val conn: HttpURLConnection = urlForDownload.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate")
        conn.connectTimeout = CONNECTION_TIMEOUT
        conn.readTimeout = CONNECTION_TIMEOUT
        val stream = if ("gzip" == conn.contentEncoding) {
            GZIPInputStream(conn.inputStream)
        } else {
            conn.inputStream
        }
        return downloadNewPatientsFromStream(stream)
    }

    private fun downloadNewPatientsFromStream(input: InputStream) {
        val reader = JsonReader(InputStreamReader(input) as Reader?)
        val realm = Realm.getInstance(realmConfig)

        try {
            val gson = JsonHelper.create()

            val sizeBatch = REALM_BATCH_SIZE
            val sizeBatchToLog = UPDATE_UI_BATCH_SIZE

            reader.beginArray()
            var totalDownloaded = 0
            var counterToLog = 0

            while (reader.hasNext()) {
                realm.executeTransaction { r ->
                    var counterToSave = 0
                    while (reader.hasNext()) {

                        counterToSave++
                        counterToLog++
                        totalDownloaded++

                        val person = gson.fromJson<fb_Person>(reader, fb_Person::class.java)
                        r.insertOrUpdate(rl_Person(person))
                        r.insertOrUpdate(RealmSyncInfo(person.updatedAt))

                        if (counterToLog == sizeBatchToLog) {
                            counterToLog = 0
                            emitter.onNext(Progress(totalDownloaded, -1))
                        }
                        if (counterToSave == sizeBatch || isInterrupted()) {
                            break
                        }
                    }
                }

                if (isInterrupted()) {
                    break
                }
            }

            finishDownload(reader, realm, emitter, if (isInterrupted()) InterruptedSyncException() else null)
        } catch (e: Exception) {
            finishDownload(reader, realm, emitter, e)
        }
    }

    private fun finishDownload(reader: JsonReader,
                               realm: Realm,
                               emitter: Emitter<Progress>,
                               error: Throwable? = null) {

        if (realm.isInTransaction) {
            realm.commitTransaction()
        }

        realm.close()
        reader.endArray()
        reader.close()
        if (error != null) {
            emitter.onError(error)
        } else {
            emitter.onComplete()
        }
    }
}
