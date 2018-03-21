package com.simprints.id.data.db.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

open class NaiveSync(private val api: SyncApiInterface,
                     private val localDbManager: LocalDbManager,
                     private val gson: Gson) {

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

    protected open fun uploadNewPatients(isInterrupted: () -> Boolean,
                                         batchSize: Int = 10): Observable<Progress> {

        val patientsToUpload = getPeopleToSync()
        val counter = AtomicInteger(0)

        return batchPatientsArray(patientsToUpload, isInterrupted, batchSize)
            .uploadEachBatch()
            .updateUploadCounterAndConvertItToProgress(counter, patientsToUpload.size)
    }

    private fun Observable<out MutableList<fb_Person>>.uploadEachBatch(): Observable<Int> =
        flatMap { patientsBatch ->
            makeUploadPatientsBatchRequest(ArrayList(patientsBatch)).toObservable()
        }

    private fun Observable<out Int>.updateUploadCounterAndConvertItToProgress(counter: AtomicInteger,
                                                                            maxValueForProgress: Int): Observable<Progress> =
        map {
            UploadProgress(counter.addAndGet(it), maxValueForProgress)
        }

    private fun batchPatientsArray(patients: ArrayList<rl_Person>,
                                   isInterrupted: () -> Boolean,
                                   batchSize: Int): Observable<MutableList<fb_Person>> {

        return Observable.fromIterable(patients)
            .takeUntil { isInterrupted() }
            .map { fb_Person(it) }
            .buffer(batchSize)
    }

    private fun getPeopleToSync(): ArrayList<rl_Person> {
        return localDbManager.getPeopleFromLocal(toSync = true)
    }

    protected open fun makeUploadPatientsBatchRequest(patientsToUpload: ArrayList<fb_Person>): Single<Int> {

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
            projectId = syncParams.projectId,
            userId = syncParams.userId,
            moduleId = syncParams.moduleId,
            toSync = false)

        return nPatientsForDownSyncQuery - nPatientsForDownSyncParamsInRealm
    }

    /**
     * Returns the total number of patients for a specific syncParams.
     * #totalPatientToDownload = #TotalPatientsFor(ProjectID, ModuleId, UserId)ComingFromServer - #TotalPatientsFor(ProjectID, ModuleId, UserId)InLocal
     */
    protected open fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> {
        return api.patientsCount("AIzaSyAoN3AsL8Qc8IdJMeZqAHmqUTipa927Jz0", syncParams.toMap())
            .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
            .map { it.patientsCount }
            .onErrorReturn { 10 }
    }

    protected open fun downloadNewPatientsFromStream(isInterrupted: () -> Boolean,
                                                     syncParams: SyncTaskParameters,
                                                     input: InputStream): Observable<Int> =

        Observable.create<Int> {
            val reader = JsonReader(InputStreamReader(input) as Reader?)

            try {
                reader.beginArray()
                var totalDownloaded = 0
                while (reader.hasNext() && !isInterrupted()) {
                    localDbManager.savePeopleFromStream(reader, gson, syncParams.toGroup()) {
                        totalDownloaded++

                        emitProgressIfRequired(it, totalDownloaded, UPDATE_UI_BATCH_SIZE)
                        isCurrentBatchDownloadedOrTaskInterrupted(
                            totalDownloaded,
                            isInterrupted,
                            LOCAL_DB_BATCH_SIZE)
                    }

                    localDbManager.updateSyncInfo(syncParams)
                }

                val possibleError = if (isInterrupted()) InterruptedSyncException() else null
                finishDownload(reader, it, possibleError)
            } catch (e: Exception) {
                finishDownload(reader, it, e)
            }
        }

    private fun isCurrentBatchDownloadedOrTaskInterrupted(totalDownloaded: Int,
                                                        isInterrupted: () -> Boolean,
                                                        maxPatientsForBatch: Int): Boolean {

        val isCurrentBatchFullyDownloaded = totalDownloaded % maxPatientsForBatch == 0
        return isCurrentBatchFullyDownloaded || isInterrupted()
    }

    private fun emitProgressIfRequired(it: ObservableEmitter<Int>,
                                       totalDownloaded: Int,
                                       emitProgressEvery: Int) {

        if (totalDownloaded % emitProgressEvery == 0) {
            it.onNext(totalDownloaded)
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
