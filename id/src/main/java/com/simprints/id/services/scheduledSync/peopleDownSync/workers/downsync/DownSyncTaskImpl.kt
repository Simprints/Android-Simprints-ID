package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.PipeSeparatorWrapperForURLListParam
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromDomainToApi
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperationResult
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperationResult.DownSyncState.FAILED
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
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

class DownSyncTaskImpl(val personLocalDataSource: PersonLocalDataSource,
                       val personRemoteDataSource: PersonRemoteDataSource,
                       private val downSyncScopeRepository: DownSyncScopeRepository,
                       val timeHelper: TimeHelper) : DownSyncTask {

    private lateinit var downSyncOperation: DownSyncOperation
    private var count = 0

    override suspend fun execute(downSyncOperation: DownSyncOperation,
                                 downSyncWorkerProgressReporter: DownSyncWorkerProgressReporter) {
        this.downSyncOperation = downSyncOperation

        var reader: JsonReader? = null
        try {
            val client = personRemoteDataSource.getPeopleApiClient()
            val response = makeDownSyncApiCallAndGetResponse(client)
            reader = setupJsonReaderFromResponse(response)
            val flowPeople = createPeopleFlowFromJsonReader(reader)
            flowPeople.bufferedChunks(BATCH_SIZE_FOR_DOWNLOADING).collect {
                saveBatchAndUpdateDownSyncStatus(it)
                count += BATCH_SIZE_FOR_DOWNLOADING
                downSyncWorkerProgressReporter.reportCount(count)
            }
            updateDownSyncInfo(COMPLETE)
        } catch (t: Throwable) {
            t.printStackTrace()
            finishDownload(reader)
            updateDownSyncInfo(FAILED)
            throw t
        }

        finishDownload(reader)
    }

    private suspend fun makeDownSyncApiCallAndGetResponse(client: PeopleRemoteInterface): ResponseBody =
        retryIO(times = RETRY_ATTEMPTS_FOR_NETWORK_CALLS) {
            with(downSyncOperation) {
                client.downSync(
                    projectId, userId, moduleId,
                    syncOperationResult?.lastPatientId,
                    syncOperationResult?.lastPatientUpdatedAt,
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
            }
        }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiGetPerson>) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople)
        Timber.d("Saved batch for $downSyncOperation")

        updateDownSyncInfo(DownSyncOperationResult.DownSyncState.RUNNING, batchOfPeople.lastOrNull(), Date())
    }

    private suspend fun updateDownSyncInfo(state: DownSyncOperationResult.DownSyncState, person: ApiGetPerson? = null, lastSyncTime: Date? = null) {
        val syncInfo = downSyncOperation.syncOperationResult
        var newSyncInfo = syncInfo?.apply {
            syncInfo.copy(lastState = state)
        } ?: DownSyncOperationResult(state, null, null, null)

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

        downSyncScopeRepository.insertOrUpdate(downSyncOperation.copy(syncOperationResult = newSyncInfo))
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
