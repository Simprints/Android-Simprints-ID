package com.simprints.id.data.db.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.DownloadProgress
import com.simprints.libcommon.Progress
import com.simprints.libcommon.UploadProgress
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

open class NaiveSync(private val api: SyncApiInterface,
                     private val localDbManager: LocalDbManager,
                     private val gson: Gson,
                     private val firebaseToken: String) {

    companion object {
        private const val LOCAL_DB_BATCH_SIZE = 10000
        const val UPDATE_UI_BATCH_SIZE = 100
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }

    fun sync(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return downloadNewPatients(isInterrupted, syncParams).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
//        return Observable.concat( //FIXME
//            uploadNewPatients(isInterrupted),
//            downloadNewPatients(isInterrupted, syncParams))
    }

    protected open fun uploadNewPatients(isInterrupted: () -> Boolean, batchSize: Int = 10): Observable<Progress> {
        val patientsToUpload = getPeopleToSync()
        val counter = AtomicInteger(0)

        return Observable.fromIterable(patientsToUpload)
            .takeUntil { isInterrupted() }
            .map { fb_Person(it) }
            .buffer(batchSize)
            .flatMap { patientsBatch ->
                uploadPatientsBatch(ArrayList(patientsBatch)).toObservable()
            }.map {
                UploadProgress(counter.addAndGet(it), patientsToUpload.size)
            }
    }

    private fun getPeopleToSync(): ArrayList<rl_Person> {
        return localDbManager.getPeopleFromLocal(toSync = true)
    }

    protected open fun uploadPatientsBatch(patientsToUpload: ArrayList<fb_Person>): Single<Int> {

        val body = gson.toJson(mapOf("patients" to patientsToUpload))
        return api.upSync("AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0", body)
            .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
            .toSingleDefault(patientsToUpload.size)
    }

    protected open fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return getNumberOfPatientsForSyncParams(syncParams).flatMapObservable { nPatientsForDownSyncQuery ->
            val nPatientsToDownload = calculateNPatientsToDownload(nPatientsForDownSyncQuery, syncParams)
            val realmSyncInfo = localDbManager.getSyncInfoFor(syncParams.toGroup())

            api.downSync(
                "AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0",
                realmSyncInfo?.lastSyncTime?.time ?: Date(0).time,
                mapOf("projectId" to syncParams.projectId)/* syncParams.toMap()*/)
                .flatMapObservable {
                    downloadNewPatientsFromStream(
                        isInterrupted,
                        syncParams,
                        it.byteStream()).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
                        .map {
                            DownloadProgress(it, nPatientsToDownload)
                        }
                }
        }
    }

    private fun calculateNPatientsToDownload(nPatientsForDownSyncQuery: Int, syncParams: SyncTaskParameters): Int {

        val nPatientsForDownSyncParamsInRealm = localDbManager.getPeopleCountFromLocal(
            null,
            syncParams.projectId,
            syncParams.userId,
            syncParams.moduleId).toInt()

        return nPatientsForDownSyncQuery - nPatientsForDownSyncParamsInRealm
    }

    /**
     * Returns the total number of patients for a specific syncParams.
     * E.g. #Patients for projectId = X, userId = Y, moduleId = Z
     *
     * The number comes from HEAD request against connector.inputStreamForDownload
     */
    private fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> {
        return api.patientsCount("AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0", syncParams.toMap()).onErrorReturn { 10 } //FIXME
            .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
    }

    protected fun downloadNewPatientsFromStream(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters, input: InputStream): Observable<Int> =
        Observable.create<Int> {
            val reader = JsonReader(InputStreamReader(input) as Reader?)

            try {
                reader.beginArray()
                var totalDownloaded = 0
                while (reader.hasNext() && !isInterrupted()) {
                    localDbManager.savePeopleFromStream(reader, gson, syncParams.toGroup()) {
                        totalDownloaded++
                        if (totalDownloaded % UPDATE_UI_BATCH_SIZE == 0) {
                            it.onNext(totalDownloaded)
                        }

                        val shouldCloseTransaction = totalDownloaded % LOCAL_DB_BATCH_SIZE == 0
                        shouldCloseTransaction || isInterrupted()
                    }
                }

                finishDownload(reader, it, if (isInterrupted()) InterruptedSyncException() else null)
            } catch (e: Exception) {
                finishDownload(reader, it, e)
            }
        }

    private fun finishDownload(reader: JsonReader,
                               emitter: Emitter<Int>,
                               error: Throwable? = null) {

        reader.endArray()
        reader.close()
        if (error != null) {
            emitter.onError(error)
        } else {
            emitter.onComplete()
        }
    }
}
