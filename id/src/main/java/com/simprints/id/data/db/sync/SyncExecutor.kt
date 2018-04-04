package com.simprints.id.data.db.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.RemoteApiInterface
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

open class SyncExecutor(private val localDbManager: LocalDbManager,
                        private val remoteDbManager: RemoteDbManager,
                        private val gson: Gson) {

    companion object {
        private const val LOCAL_DB_BATCH_SIZE = 10000
        const val UPDATE_UI_BATCH_SIZE = 100
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }

    private val syncApi: RemoteApiInterface by lazy {
        remoteDbManager.getSyncApi().blockingGet()
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

        Timber.d("Uploading ${patientsToUpload.size} people")
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
        return localDbManager.loadPeopleFromLocal(toSync = true)
    }

    protected open fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {

        return remoteDbManager.getNumberOfPatientsForSyncParams(syncParams).flatMapObservable { nPatientsForDownSyncQuery ->
            val nPeopleToDownload = calculateNPatientsToDownload(nPatientsForDownSyncQuery, syncParams)

            Timber.d("Downloading batch $nPeopleToDownload people")
            val realmSyncInfo = localDbManager.getSyncInfoFor(syncParams.toGroup())
                ?: RealmSyncInfo(syncParams.toGroup())

            syncApi.downSync(
                realmSyncInfo.lastSyncTime.time,
                syncParams.toMap())
                .flatMapObservable {
                    savePeopleFromStream(
                        isInterrupted,
                        syncParams,
                        it.byteStream()).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
                        .map {
                            DownloadProgress(it, nPeopleToDownload)
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

    protected open fun savePeopleFromStream(isInterrupted: () -> Boolean,
                                            syncParams: SyncTaskParameters,
                                            input: InputStream): Observable<Int> =
        Observable.create<Int> { result ->

            val peopleInserted = hashMapOf<String, Int>()

            val reader = JsonReader(InputStreamReader(input) as Reader?)
            try {
                reader.beginArray()
                var totalDownloaded = 0
                while (reader.hasNext() && !isInterrupted()) {
                    localDbManager.savePeopleFromStreamAndUpdateSyncInfo(reader, gson, syncParams) {
                        if (!peopleInserted.containsKey(it.patientId)) {
                            peopleInserted[it.patientId] = 1
                        } else {
                            Timber.d("Duplicate!!!!!!! ${it.patientId}")
                        }

                        totalDownloaded++
                        emitResultProgressIfRequired(result, totalDownloaded, UPDATE_UI_BATCH_SIZE)
                        val shouldDownloadingBatchStop = isInterrupted() || hasCurrentBatchDownloadedFinished(totalDownloaded, LOCAL_DB_BATCH_SIZE)
                        shouldDownloadingBatchStop
                    }
                }

                val possibleError = if (isInterrupted()) InterruptedSyncException() else null
                finishDownload(reader, result, possibleError)
            } catch (e: Exception) {
                finishDownload(reader, result, e)
            }
        }

    private fun hasCurrentBatchDownloadedFinished(totalDownloaded: Int,
                                                  maxPatientsForBatch: Int): Boolean {

        return totalDownloaded % maxPatientsForBatch == 0
    }

    private fun emitResultProgressIfRequired(it: ObservableEmitter<Int>,
                                             totalDownloaded: Int,
                                             emitProgressEvery: Int) {

        if (totalDownloaded % emitProgressEvery == 0) {
            it.onNext(totalDownloaded)
        }
    }

    private fun finishDownload(reader: JsonReader,
                               emitter: Emitter<Int>,
                               error: Throwable? = null) {

        Timber.d("Download finished")

        reader.endArray()
        reader.close()
        if (error != null) {
            emitter.onError(error)
        } else {
            emitter.onComplete()
        }
    }
}
