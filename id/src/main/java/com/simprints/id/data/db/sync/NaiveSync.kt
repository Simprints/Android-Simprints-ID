package com.simprints.id.data.db.sync

import com.google.gson.stream.JsonReader
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Constants
import com.simprints.id.sync.models.RealmSyncInfo
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.Progress
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class NaiveSync(private val isInterrupted: () -> Boolean,
                private val syncGroup: Constants.GROUP,
                private val connector: NaiveSyncConnector,
                private val realmConfig: RealmConfiguration) {

    companion object {
        private const val LOCAL_DB_BATCH_SIZE = 10000
        private const val UPDATE_UI_BATCH_SIZE = 100
    }

    fun sync(): Observable<Progress> {
        return downloadNewPatients()
    }

    private fun downloadNewPatients(): Observable<Progress> {
        return Observable.create<Progress> {
            downloadNewPatientsFromStream(connector.inputStreamForDownload, it)
        }
    }

    private fun downloadNewPatientsFromStream(input: InputStream, emitter: ObservableEmitter<Progress>) {
        val reader = JsonReader(InputStreamReader(input) as Reader?)
        val realm = Realm.getInstance(realmConfig)

        try {
            val gson = JsonHelper.create()

            reader.beginArray()
            var totalDownloaded = 0

            while (reader.hasNext() && !isInterrupted()) {
                realm.executeTransaction { r ->
                    while (reader.hasNext()) {
                        val person = gson.fromJson<fb_Person>(reader, fb_Person::class.java)
                        r.insertOrUpdate(rl_Person(person))
                        r.insertOrUpdate(RealmSyncInfo(syncGroup.ordinal, person.updatedAt))
                        totalDownloaded++

                        if (totalDownloaded % UPDATE_UI_BATCH_SIZE == 0) {
                            emitter.onNext(Progress(totalDownloaded, 0))
                        }

                        if (totalDownloaded % LOCAL_DB_BATCH_SIZE == 0 || isInterrupted()) {
                            break
                        }
                    }
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
