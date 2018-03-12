package com.simprints.id.data.db.sync

import com.google.gson.stream.JsonReader
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
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
                val projectId: String,
                val moduleId: String?,
                val userId: String?,
                private val realmConfig: RealmConfiguration) {

    private val urlForDownload by lazy {
        val builder = HttpUrl.Builder()
            .scheme("https")
            .host("sync-manager-dot-simprints-dev.appspot.com")
            .addPathSegment("patients-sync")
            .addQueryParameter("key", "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0")
            .addQueryParameter("projectId", "testProjectWith100kPatients")
            .addQueryParameter("batchSize", 5000.toString())

        moduleId?.let {
            builder.addQueryParameter("moduleId", it)
        }
        userId?.let {
            builder.addQueryParameter("userId", it)
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
        conn.connectTimeout = 40 * 1000
        conn.readTimeout = 40 * 1000
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

            val sizeBatch = 10000
            val sizeBatchToLog = 100

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
