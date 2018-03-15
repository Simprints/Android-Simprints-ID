package com.simprints.id.data.db.sync

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.local.models.rl_Person
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
import java.lang.Math.min
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
        val patientsToUpload = localDbManager.getPeopleToUpSync()
        val batchSize = 10
        return makeUploadRequest(isInterrupted, patientsToUpload, batchSize).map {
            DownloadProgress(it, patientsToUpload.size / batchSize)
        }
    }

    private fun makeUploadRequest(isInterrupted: () -> Boolean,
                                  patientsToUpload: ArrayList<rl_Person>,
                                  batchSize: Int = 2000): Observable<Int> {

        val counterBatch = AtomicInteger()
        val gson = JsonHelper.create()

        return Observable.fromIterable(patientsToUpload)
            .takeUntil { !isInterrupted() }
            .map { fb_Person(it) }
            .buffer(batchSize)
            .map { subGroup ->
                val body = gson.toJson(mapOf("patients" to subGroup))
                api.upSync(body).retry(5)
            }.map {
                counterBatch.addAndGet(min(patientsToUpload.size, batchSize))
            }
    }

    private fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return getNumberOfPatientsForSyncParams(syncParams).flatMapObservable { nPatientsForDownSyncQuery ->
            val nPatientsToDownload = calculateNPatientsToDownload(nPatientsForDownSyncQuery, syncParams)
            val realmSyncInfo = localDbManager.getSyncInfoFor(syncParams.toGroup())

            api.downSync(
                "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0",
                realmSyncInfo?.lastSyncTime ?: Date(0),
                syncParams.toMap()).flatMapObservable {
                downloadNewPatientsFromStream(
                    isInterrupted,
                    syncParams,
                    it.byteStream()).retry(5)
                    .map {
                        DownloadProgress(it, nPatientsToDownload)
                    }
            }
        }
    }

    private fun calculateNPatientsToDownload(nPatientsForDownSyncQuery: Int, syncParams: SyncTaskParameters): Int {

        val nPatientsForDownSyncParamsInRealm = localDbManager.getPeopleFor(syncParams).count()
        return nPatientsForDownSyncQuery - nPatientsForDownSyncParamsInRealm
    }

    /**
     * Returns the total number of patients for a specific syncParams.
     * E.g. #Patients for projectId = X, userId = Y, moduleId = Z
     *
     * The number comes from HEAD request against connector.inputStreamForDownload
     */
    private fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> {
        return api.patientsCount("AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0", syncParams.toMap())
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
