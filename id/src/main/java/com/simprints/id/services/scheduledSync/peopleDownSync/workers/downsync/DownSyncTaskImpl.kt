package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.syncscope.DownSyncScopeRepository
import com.simprints.id.data.db.syncscope.domain.DownSyncInfo
import com.simprints.id.data.db.syncscope.domain.DownSyncOperation
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader

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
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            finishDownload(reader)
        }
    }

    private suspend fun makeDownSyncApiCallAndGetResponse(client: PeopleRemoteInterface): ResponseBody =
        retryIO(times = RETRY_ATTEMPTS_FOR_NETWORK_CALLS) {
            with(downSyncOperation) {
                client.downSync(
                    projectId, userId, moduleId,
                    syncInfo?.lastPatientId,
                    syncInfo?.lastPatientUpdatedAt)
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

        updateDownSyncInfo(batchOfPeople.last())
    }

    private fun updateDownSyncInfo(lastPatient: ApiGetPerson) {
        val newDownSyncInfo = lastPatient.updatedAt?.time?.let {
            DownSyncInfo(
                    DownSyncInfo.DownSyncState.RUNNING,
                    lastPatient.id,
                    it,
                    timeHelper.now())
        }
        downSyncOperation = downSyncOperation.copy(syncInfo = newDownSyncInfo)
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
