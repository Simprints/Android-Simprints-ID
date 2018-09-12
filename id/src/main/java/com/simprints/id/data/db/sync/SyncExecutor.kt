package com.simprints.id.data.db.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.DownSyncParams
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.exceptions.safe.InterruptedSyncException
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.*
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.atomic.AtomicInteger

open class SyncExecutor(private val dbManager: DbManager,
                        private val gson: Gson) {

    companion object {
        private const val DOWN_BATCH_SIZE_FOR_DOWNLOADING = 10000
        const val DOWN_BATCH_SIZE_FOR_UPDATING_UI = 100
        const val UP_BATCH_SIZE = 80
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }

    private val syncApi: PeopleRemoteInterface by lazy {
        dbManager.remote.getPeopleApiClient().blockingGet()
    }

    fun sync(isInterrupted: () -> Boolean = { false }, syncParams: SyncTaskParameters): Observable<Progress> {
        Timber.d("Sync Started")
        return Observable.concat(
            uploadNewPatients(isInterrupted, syncParams),
            downloadNewPatients(isInterrupted, syncParams))
    }

    protected open fun uploadNewPatients(isInterrupted: () -> Boolean,
                                         syncParams: SyncTaskParameters,
                                         batchSize: Int = UP_BATCH_SIZE): Observable<Progress> =
        getPeopleCountToSync().flatMapObservable {
            val counter = AtomicInteger(0)

            Timber.d("Uploading $it people")
            getPeopleInBatches(isInterrupted, batchSize)
                .uploadEachBatch(syncParams.projectId)
                .updateUploadCounterAndConvertItToProgress(counter, it)
                .toObservable()
        }

    private fun Flowable<out MutableList<fb_Person>>.uploadEachBatch(projectId: String): Flowable<Int> =
        flatMap { batch ->
            dbManager.remote
                .uploadPeople(projectId, ArrayList(batch))
                .andThen(Flowable.just(batch.size))
        }

    private fun Flowable<out Int>.updateUploadCounterAndConvertItToProgress(counter: AtomicInteger, maxValueForProgress: Int): Flowable<Progress> =
        map {
            Timber.d("Uploading batch - ${counter.get() + 1} / $maxValueForProgress")
            UploadProgress(counter.addAndGet(it), maxValueForProgress)
        }

    private fun getPeopleInBatches(isInterrupted: () -> Boolean,
                                   batchSize: Int): Flowable<MutableList<fb_Person>> =
        dbManager.local.loadPeopleFromLocalRx(toSync = true)
            .takeUntil { isInterrupted() }
            .map { fb_Person(it) }
            .buffer(batchSize)

    private fun getPeopleCountToSync(): Single<Int> =
        dbManager.local.getPeopleCountFromLocal(toSync = true)

    protected open fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> =
        dbManager.remote.getNumberOfPatientsForSyncParams(syncParams).flatMap {
            dbManager.calculateNPatientsToDownSync(it, syncParams)
        }.flatMapObservable { nPeopleToDownload ->
            Timber.d("Downloading batch $nPeopleToDownload people")
            val downSyncParam = DownSyncParams(syncParams, dbManager.local)
            syncApi.downSync(
                downSyncParam.projectId,
                downSyncParam.userId,
                downSyncParam.moduleId,
                downSyncParam.lastKnownPatientId,
                downSyncParam.lastKnownPatientUpdatedAt)
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

    protected open fun savePeopleFromStream(isInterrupted: () -> Boolean,
                                            syncParams: SyncTaskParameters,
                                            input: InputStream): Observable<Int> =
        Observable.create<Int> { result ->

            val reader = JsonReader(InputStreamReader(input) as Reader?)
            try {
                reader.beginArray()
                var totalDownloaded = 0
                while (reader.hasNext() && !isInterrupted()) {
                    dbManager.local.savePeopleFromStreamAndUpdateSyncInfo(reader, gson, syncParams) {
                        totalDownloaded++
                        emitResultProgressIfRequired(result, totalDownloaded, DOWN_BATCH_SIZE_FOR_UPDATING_UI)
                        val shouldDownloadingBatchStop = isInterrupted() ||
                                                                  hasCurrentBatchDownloadedFinished(totalDownloaded, DOWN_BATCH_SIZE_FOR_DOWNLOADING)
                        shouldDownloadingBatchStop
                    }.subscribe()
                }

                val possibleError = if (isInterrupted()) InterruptedSyncException() else null
                finishDownload(reader, result, possibleError)
            } catch (e: Exception) {
                finishDownload(reader, result, e)
            }
        }

    private fun hasCurrentBatchDownloadedFinished(totalDownloaded: Int, maxPatientsForBatch: Int) =
        totalDownloaded % maxPatientsForBatch == 0

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
