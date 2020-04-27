package com.simprints.id.services.scheduledSync.people.down.workers

import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.TimeHelper

class PeopleDownSyncDownloaderTaskImpl(val personLocalDataSource: PersonLocalDataSource,
                                       private val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                       private val cache: PeopleSyncCache,
                                       val timeHelper: TimeHelper) : PeopleDownSyncDownloaderTask {

    private lateinit var downSyncOperation: PeopleDownSyncOperation
    private lateinit var downSyncWorkerProgressReporter: WorkerProgressCountReporter

    private var count = 0
    override suspend fun execute(downSyncOperation: PeopleDownSyncOperation,
                                 workerId: String,
                                 downSyncWorkerProgressReporter: WorkerProgressCountReporter): Int = 0 }


//        return withContext(Dispatchers.IO) {
//            this@PeopleDownSyncDownloaderTaskImpl.downSyncOperation = downSyncOperation
//            this@PeopleDownSyncDownloaderTaskImpl.downSyncWorkerProgressReporter = downSyncWorkerProgressReporter
//
//            count = cache.readProgress(workerId)
//            downSyncWorkerProgressReporter.reportCount(count)
//
//            var reader: JsonReader? = null
//            val bufferToSave = mutableListOf<ApiGetPerson>()
//
//            try {
//                val client = personRemoteDataSource.getPeopleApiClient()
//                val response = makeDownSyncApiCallAndGetResponse(client)
//                reader = setupJsonReaderFromResponse(response)
//
//                val channelFromNetwork = createPeopleChannelFromJsonReader(reader)
//                while (!channelFromNetwork.isClosedForReceive) {
//                    channelFromNetwork.poll()?.let {
//                        bufferToSave.add(it)
//                        if (bufferToSave.size > BATCH_SIZE_FOR_DOWNLOADING) {
//                            saveBatch(workerId, bufferToSave)
//                        }
//                    }
//                }
//
//                saveBatch(workerId, bufferToSave)
//                updateDownSyncInfo(COMPLETE)
//
//            } catch (t: Throwable) {
//                t.printStackTrace()
//                saveBatch(workerId, bufferToSave)
//                finishDownload(reader)
//                updateDownSyncInfo(FAILED)
//                throw t
//            }
//
//            finishDownload(reader)
//            count
//        }
//    }
//
//    private suspend fun saveBatch(workerId: String, batch: MutableList<ApiGetPerson>) {
//        saveBatchAndUpdateDownSyncStatus(batch)
//        updateCounters(workerId, batch.size)
//        batch.clear()
//    }
//
//    private suspend fun updateCounters(workerId: String, newElementsCount: Int) {
//        count += newElementsCount
//        cache.saveProgress(workerId, count)
//        downSyncWorkerProgressReporter.reportCount(count)
//    }
//
//    private suspend fun makeDownSyncApiCallAndGetResponse(client: EventRemoteInterface): ResponseBody =
//        retrySimNetworkCalls(client, {
//            with(downSyncOperation) {
//                client.downSync(
//                    projectId, userId, moduleId,
//                    lastResult?.lastPatientId,
//                    lastResult?.lastPatientUpdatedAt,
//                    PipeSeparatorWrapperForURLListParam(*modes.map { it.fromDomainToApi() }.toTypedArray()))
//
//            }
//        }, "downSync")
//
//    private fun setupJsonReaderFromResponse(response: ResponseBody): JsonReader =
//        JsonReader(InputStreamReader(response.byteStream()) as Reader?)
//            .also {
//                it.beginArray()
//            }
//
//
//    private fun CoroutineScope.createPeopleChannelFromJsonReader(reader: JsonReader) = produce<ApiGetPerson>(capacity = 5 * BATCH_SIZE_FOR_DOWNLOADING) {
//        while (reader.hasNext()) {
//            this.send(JsonHelper.gson.fromJson(reader, ApiGetPerson::class.java))
//        }
//    }
//
//    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiGetPerson>) {
//        filterBatchOfPeopleToSyncWithLocal(batchOfPeople)
//        Timber.tag(SYNC_LOG_TAG).d("Saved batch(${batchOfPeople.size}) for $downSyncOperation")
//
//        updateDownSyncInfo(PeopleDownSyncOperationResult.DownSyncState.RUNNING, batchOfPeople.lastOrNull(), Date())
//    }
//
//    private suspend fun updateDownSyncInfo(state: PeopleDownSyncOperationResult.DownSyncState,
//                                           person: ApiGetPerson? = null,
//                                           lastSyncTime: Date? = null) {
//        var newResultInfo = downSyncOperation.lastResult?.copy(state = state)
//            ?: PeopleDownSyncOperationResult(state, null, null, null)
//
//        if (person != null) {
//            newResultInfo = person.let {
//                newResultInfo.copy(
//                    lastPatientId = person.id,
//                    lastPatientUpdatedAt = person.updatedAt?.time)
//            }
//        }
//
//        if (lastSyncTime != null) {
//            newResultInfo = lastSyncTime.let {
//                newResultInfo.copy(
//                    lastSyncTime = it.time)
//            }
//        }
//        downSyncOperation = downSyncOperation.copy(lastResult = newResultInfo)
//        downSyncScopeRepository.insertOrUpdate(downSyncOperation)
//    }
//
//
//    private fun finishDownload(reader: JsonReader?) {
//        Timber.tag(SYNC_LOG_TAG).d("Download finished")
//        reader?.endArray()
//        reader?.close()
//    }
//
//    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfPeople: List<ApiGetPerson>) {
//        val batchOfPeopleToSaveInLocal = batchOfPeople.filter { !it.deleted }
//        val batchOfPeopleToBeDeleted = batchOfPeople.filter { it.deleted }
//
//        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
//        deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted)
//    }
//
//    private suspend fun savePeopleBatchInLocal(batchOfPeopleToSaveInLocal: List<ApiGetPerson>) {
//        if (batchOfPeopleToSaveInLocal.isNotEmpty()) {
//            personLocalDataSource.insertOrUpdate(batchOfPeopleToSaveInLocal.map { it.fromGetApiToDomain() })
//        }
//    }
//
//    private suspend fun deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted: List<ApiGetPerson>) {
//        if (batchOfPeopleToBeDeleted.isNotEmpty()) {
//            personLocalDataSource.delete(buildQueryForPeopleById(batchOfPeopleToBeDeleted))
//        }
//    }
//
//    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<ApiGetPerson>) =
//        batchOfPeopleToBeDeleted.map {
//            PersonLocalDataSource.Query(personId = it.id)
//        }
//
//    companion object {
//        const val BATCH_SIZE_FOR_DOWNLOADING = 200
//    }
//}
