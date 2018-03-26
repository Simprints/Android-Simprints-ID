package com.simprints.id.data.db.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

open class NaiveSync(private val localDbManager: LocalDbManager,
                     private val remoteDbManager: RemoteDbManager,
                     private val gson: Gson) {

    companion object {
        private const val LOCAL_DB_BATCH_SIZE = 10000
        const val UPDATE_UI_BATCH_SIZE = 100
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }

    fun sync(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        Timber.d("Sync Started")
        return Observable.concat(
            uploadNewPatients(isInterrupted),
            downloadNewPatients(isInterrupted, syncParams))
    }

    protected open fun uploadNewPatients(isInterrupted: () -> Boolean,
                                         batchSize: Int = 10): Observable<Progress> {

        val patientsToUpload = getPeopleToSync()
        val counter = AtomicInteger(0)

        Timber.d("Uploading ${patientsToUpload.size} persons")
        return batchPatientsArray(patientsToUpload, isInterrupted, batchSize)
            .uploadEachBatch()
            .updateUploadCounterAndConvertItToProgress(counter, patientsToUpload.size)
    }

     private fun Observable<out MutableList<fb_Person>>.uploadEachBatch(): Observable<Int> =
        flatMap { batch ->
            remoteDbManager
                .uploadPeople(ArrayList(batch))
                .andThen(Observable.just(batch.size))
        }

    private fun Observable<out Int>.updateUploadCounterAndConvertItToProgress(counter: AtomicInteger, maxValueForProgress: Int): Observable<Progress> =
        map {
            Timber.d("Uploading batch - ${counter.get() + 1} / $maxValueForProgress")
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
        return localDbManager.loadPersonsFromLocal(toSync = true)
    }

    protected open fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        Timber.d("Downloading - reading from db")

        return remoteDbManager.getNumberOfPatientsForSyncParams(syncParams).flatMapObservable { nPatientsForDownSyncQuery ->
            val nPatientsToDownload = calculateNPatientsToDownload(nPatientsForDownSyncQuery, syncParams)

            Timber.d("Downloading - Persons to be downloaded $nPatientsToDownload.")

            remoteDbManager.getSyncApi().flatMapObservable {
                Timber.d("Downloading - Making request")
                val realmSyncInfo = localDbManager.getSyncInfoFor(syncParams.toGroup())
                Timber.d("Downloading - Last sync was at ${realmSyncInfo?.lastSyncTime?.time ?: Date(0).time}")

                it.downSync(
                    realmSyncInfo?.lastSyncTime?.time ?: Date(0).time,
                    syncParams.toMap())
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
    }

    private fun calculateNPatientsToDownload(nPatientsForDownSyncQuery: Int, syncParams: SyncTaskParameters): Int {

        val nPatientsForDownSyncParamsInRealm = localDbManager.getPersonsCountFromLocal(
            projectId = syncParams.projectId,
            userId = syncParams.userId,
            moduleId = syncParams.moduleId,
            toSync = false)

        return nPatientsForDownSyncQuery - nPatientsForDownSyncParamsInRealm
    }

    protected open fun downloadNewPatientsFromStream(isInterrupted: () -> Boolean,
                                                     syncParams: SyncTaskParameters,
                                                     input: InputStream): Observable<Int> =

        Observable.create<Int> {
            Timber.d("Downloading - Reading from stream")

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
