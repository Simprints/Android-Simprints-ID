package com.simprints.id.data.db.sync

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.DownloadProgress
import com.simprints.libcommon.Progress
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class NaiveSync(private val api: SimApiInterface,
                private val realmConfig: RealmConfiguration,
                private val localDbManager: LocalDbManager) {

    companion object {
        private const val LOCAL_DB_BATCH_SIZE = 10000
        private const val UPDATE_UI_BATCH_SIZE = 100
    }

    fun sync(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return Observable.concat(
            uploadNewPatients(isInterrupted),
            downloadNewPatients(isInterrupted, syncParams))
    }

    private fun uploadNewPatients(isInterrupted: () -> Boolean): Observable<Progress> {
        val patientsToUpload = localDbManager.getPatientsToUpSync()
        return makeUploadRequest(isInterrupted, patientsToUpload)
    }

    private fun makeUploadRequest(isInterrupted: () -> Boolean, patientsToUpload: ArrayList<rl_Person>): Observable<Progress> {
        return Observable.just(Progress(0, 0))
    }
//        subGroupOfPatients ->
//            val gson = JsonHelper.create()
//            val fbPatients = arrayListOf<fb_Person>()
//            subGroupOfPatients.forEach { fbPatients.add(fb_Person(it)) }
//            val patientsJson = gson.toJson(mapOf("patients" to fbPatients))
//            api.upSync(patientsJson)
//        }
//    }

    private fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return getNumberOfPatientsForDownloadQuery(syncParams).flatMapObservable { nPatientsForDownSyncQuery ->
            val nPatientsToDownload = calculateNPatientsToDownload(nPatientsForDownSyncQuery)
            val lastSyncTime = Date(0)//LastSyncTime

            api.downSync(
                "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0",
                lastSyncTime,
                syncParams.projectId).flatMapObservable {
                    downloadNewPatientsFromStream(
                        isInterrupted,
                        syncParams,
                        it.byteStream())
                        .map {
                            DownloadProgress(it, nPatientsToDownload)
                        }
                }
        }
    }

    private fun calculateNPatientsToDownload(nPatientsForDownSyncQuery: Int): Int {
        // TODO: Implement
        // The patients we download is equal to #patientsForQuery - #patientsForQueryInDbSinceTimeStamp
        val nPatientsForDownSyncQueryInRealm = 0 // Realm.findAll.with(syncParams)
        return nPatientsForDownSyncQuery - nPatientsForDownSyncQueryInRealm
    }

    /**
     * Returns the total number of patients for a specific syncParams.
     * E.g. #Patients for projectId = X, userId = Y, moduleId = Z
     *
     * The number comes from HEAD request against connector.inputStreamForDownload
     */
    private fun getNumberOfPatientsForDownloadQuery(syncParams: SyncTaskParameters): Single<Int> {
        // TODO: Implement
        return Single.just(0)
    }

    private fun downloadNewPatientsFromStream(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters, input: InputStream): Observable<Int> =
        Observable.create<Int> {
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
                            r.insertOrUpdate(RealmSyncInfo(syncParams.toGroup().ordinal, person.updatedAt))
                            totalDownloaded++

                            if (totalDownloaded % UPDATE_UI_BATCH_SIZE == 0) {
                                it.onNext(totalDownloaded)
                            }

                            val shouldCloseTransaction = totalDownloaded % LOCAL_DB_BATCH_SIZE == 0
                            if (shouldCloseTransaction || isInterrupted()) {
                                break
                            }
                        }
                    }
                }

                finishDownload(reader, realm, it, if (isInterrupted()) InterruptedSyncException() else null)
            } catch (e: Exception) {
                finishDownload(reader, realm, it, e)
            }
        }

    private fun finishDownload(reader: JsonReader,
                               realm: Realm,
                               emitter: Emitter<Int>,
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
