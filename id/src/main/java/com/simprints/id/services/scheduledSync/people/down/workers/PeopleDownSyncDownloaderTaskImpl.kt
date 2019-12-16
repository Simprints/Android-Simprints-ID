package com.simprints.id.services.scheduledSync.people.down.workers

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.FAILED
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.PipeSeparatorWrapperForURLListParam
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromDomainToApi
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class PeopleDownSyncDownloaderTaskImpl(val personLocalDataSource: PersonLocalDataSource,
                                       val personRemoteDataSource: PersonRemoteDataSource,
                                       private val downSyncScopeRepository: DownSyncScopeRepository,
                                       val timeHelper: TimeHelper) : PeopleDownSyncDownloaderTask {

    private lateinit var downSyncOperation: PeopleDownSyncOperation
    private lateinit var downSyncWorkerProgressReporter: WorkerProgressCountReporter

    private var count = 0

    override suspend fun execute(downSyncOperation: PeopleDownSyncOperation,
                                 downSyncWorkerProgressReporter: WorkerProgressCountReporter): Int {
        this.downSyncOperation = downSyncOperation
        this.downSyncWorkerProgressReporter = downSyncWorkerProgressReporter

        var reader: JsonReader? = null
        try {
            val client = personRemoteDataSource.getPeopleApiClient()
            val response = makeDownSyncApiCallAndGetResponse(client)
            reader = setupJsonReaderFromResponse(response)
            val flowPeople = createPeopleFlowFromJsonReader(reader)
            flowPeople.bufferedChunks(BATCH_SIZE_FOR_DOWNLOADING).collect {
                saveBatchAndUpdateDownSyncStatus(it)
            }
            updateDownSyncInfo(COMPLETE)
        } catch (t: Throwable) {
            t.printStackTrace()
            finishDownload(reader)
            updateDownSyncInfo(FAILED)
            throw t
        }

        finishDownload(reader)
        return count
    }

    private suspend fun makeDownSyncApiCallAndGetResponse(client: PeopleRemoteInterface): ResponseBody =
        retryIO(times = RETRY_ATTEMPTS_FOR_NETWORK_CALLS) {
            with(downSyncOperation) {
                client.downSync(
                    projectId, userId, moduleId,
                    lastResult?.lastPatientId,
                    lastResult?.lastPatientUpdatedAt,
                    PipeSeparatorWrapperForURLListParam(*modes.map { it.fromDomainToApi() }.toTypedArray()))
            }
        }

    private fun setupJsonReaderFromResponse(response: ResponseBody): JsonReader =
        JsonReader(InputStreamReader(response.byteStream()) as Reader?)
            .also {
                it.beginArray()
            }


    private fun createPeopleFlowFromJsonReader(reader: JsonReader): Flow<ApiGetPerson> =
        flow {
            while (reader.hasNext()) {
                this.emit(JsonHelper.gson.fromJson(reader, ApiGetPerson::class.java))
                count += 1
                downSyncWorkerProgressReporter.reportCount(count)
            }
        }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiGetPerson>) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople)
        Timber.d("Saved batch for $downSyncOperation")

        updateDownSyncInfo(PeopleDownSyncOperationResult.DownSyncState.RUNNING, batchOfPeople.lastOrNull(), Date())
    }

    private suspend fun updateDownSyncInfo(state: PeopleDownSyncOperationResult.DownSyncState,
                                           person: ApiGetPerson? = null,
                                           lastSyncTime: Date? = null) {
        val syncInfo = downSyncOperation.lastResult
        var newSyncInfo = syncInfo?.apply {
            syncInfo.copy(lastState = state)
        } ?: PeopleDownSyncOperationResult(state, null, null, null)

        if (person != null) {
            newSyncInfo = person.let {
                newSyncInfo.copy(
                    lastPatientId = person.id,
                    lastPatientUpdatedAt = person.updatedAt?.time)
            }
        }

        if (lastSyncTime != null) {
            newSyncInfo = lastSyncTime.let {
                newSyncInfo.copy(
                    lastSyncTime = it.time)
            }
        }
        downSyncOperation = downSyncOperation.copy(lastResult = newSyncInfo)
        downSyncScopeRepository.insertOrUpdate(downSyncOperation)
    }


    private fun finishDownload(reader: JsonReader?) {
        Timber.d("Download finished")
        reader?.endArray()
        reader?.close()
    }

    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfPeople: List<ApiGetPerson>) {
        val batchOfPeopleToSaveInLocal = batchOfPeople.filter { !it.deleted }
        val batchOfPeopleToBeDeleted = batchOfPeople.filter { it.deleted }

        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
        deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted)
    }

    private suspend fun savePeopleBatchInLocal(batchOfPeopleToSaveInLocal: List<ApiGetPerson>) {
        if (batchOfPeopleToSaveInLocal.isNotEmpty()) {
            personLocalDataSource.insertOrUpdate(batchOfPeopleToSaveInLocal.map { it.fromGetApiToDomain() })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted: List<ApiGetPerson>) {
        if (batchOfPeopleToBeDeleted.isNotEmpty()) {
            personLocalDataSource.delete(buildQueryForPeopleById(batchOfPeopleToBeDeleted))
        }
    }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<ApiGetPerson>) =
        batchOfPeopleToBeDeleted.map {
            PersonLocalDataSource.Query(personId = it.id)
        }

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
