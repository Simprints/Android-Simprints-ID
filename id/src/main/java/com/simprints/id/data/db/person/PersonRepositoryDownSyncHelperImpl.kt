package com.simprints.id.data.db.person

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.person.domain.personevents.Event
import com.simprints.id.data.db.person.domain.personevents.fromApiToDomain
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.services.scheduledSync.people.common.SYNC_LOG_TAG
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class PersonRepositoryDownSyncHelperImpl(val personLocalDataSource: PersonLocalDataSource,
                                         val eventRemoteDataSource: EventRemoteDataSource,
                                         private val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                         val timeHelper: TimeHelper) : PersonRepositoryDownSyncHelper {

    private lateinit var downSyncOperation: PeopleDownSyncOperation

    override suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                                     downSyncOperation: PeopleDownSyncOperation,
                                                     eventQuery: EventQuery): ReceiveChannel<Int> =
        scope.produce {

            this@PersonRepositoryDownSyncHelperImpl.downSyncOperation = downSyncOperation
            var reader: JsonReader? = null

            val bufferToSave = mutableListOf<ApiEvent>()

            try {
                val response = makeDownSyncApiCallAndGetResponse(eventQuery)
                reader = setupJsonReaderFromResponse(response)

                val channelFromNetwork = createPeopleChannelFromJsonReader(reader)
                while (!channelFromNetwork.isClosedForReceive) {
                    channelFromNetwork.poll()?.let {
                        bufferToSave.add(it)
                        if (bufferToSave.size > BATCH_SIZE_FOR_DOWNLOADING) {
                            saveBatch(bufferToSave)
                        }
                    }
                }

                saveBatch(bufferToSave)
                updateDownSyncInfo(PeopleDownSyncOperationResult.DownSyncState.COMPLETE)

            } catch (t: Throwable) {
                t.printStackTrace()
                saveBatch(bufferToSave)
                finishDownload(reader)
                updateDownSyncInfo(PeopleDownSyncOperationResult.DownSyncState.FAILED)
                throw t
            }

            finishDownload(reader)
        }

    private suspend fun makeDownSyncApiCallAndGetResponse(eventQuery: EventQuery) =
        eventRemoteDataSource.get(eventQuery)

    private fun setupJsonReaderFromResponse(response: ResponseBody): JsonReader =
        JsonReader(InputStreamReader(response.byteStream()) as Reader?)
            .also {
                it.beginArray()
            }

    private fun CoroutineScope.createPeopleChannelFromJsonReader(reader: JsonReader) = produce<ApiEvent>(capacity = 5 * PeopleDownSyncDownloaderTaskImpl.BATCH_SIZE_FOR_DOWNLOADING) {
        while (reader.hasNext()) {
            this.send(JsonHelper.gson.fromJson(reader, ApiEvent::class.java))
        }
    }

    private suspend fun saveBatch(batch: MutableList<ApiEvent>) {
        saveBatchAndUpdateDownSyncStatus(batch)
        batch.clear()
    }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiEvent>) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople)
        Timber.tag(SYNC_LOG_TAG).d("Saved batch(${batchOfPeople.size}) for $downSyncOperation")

        updateDownSyncInfo(PeopleDownSyncOperationResult.DownSyncState.RUNNING, batchOfPeople.lastOrNull(), Date())
    }

    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfEvents: List<ApiEvent>) {
        val batchOfPeopleToSaveInLocal =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordCreationPayload }.map {
                it.fromApiToDomain()
            }

        val batchOfPeopleToBeDeleted =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordDeletionPayload }.map {
                it.fromApiToDomain()
            }

        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
        deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted)
    }

    private suspend fun savePeopleBatchInLocal(batchOfPeopleToSaveInLocal: List<Event>) {
        if (batchOfPeopleToSaveInLocal.isNotEmpty()) {
            personLocalDataSource.insertOrUpdate(batchOfPeopleToSaveInLocal.map { it.fromGetApiToDomain() })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted: List<Event>) {
        if (batchOfPeopleToBeDeleted.isNotEmpty()) {
            personLocalDataSource.delete(buildQueryForPeopleById(batchOfPeopleToBeDeleted))
        }
    }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<ApiGetPerson>) =
        batchOfPeopleToBeDeleted.map {
            PersonLocalDataSource.Query(personId = it.id)
        }

    private suspend fun updateDownSyncInfo(state: PeopleDownSyncOperationResult.DownSyncState,
                                           event: ApiEvent? = null,
                                           lastSyncTime: Date? = null) {
        var newResultInfo = downSyncOperation.lastResult?.copy(state = state)
            ?: PeopleDownSyncOperationResult(state, null, null, null)

        if (event != null) {
            newResultInfo = event.let {
                newResultInfo.copy(
                    lastPatientId = event.id,
                    lastPatientUpdatedAt = event.updatedAt?.time)
            }
        }

        if (lastSyncTime != null) {
            newResultInfo = lastSyncTime.let {
                newResultInfo.copy(
                    lastSyncTime = it.time)
            }
        }
        downSyncOperation = downSyncOperation.copy(lastResult = newResultInfo)
        downSyncScopeRepository.insertOrUpdate(downSyncOperation)
    }

    private fun finishDownload(reader: JsonReader?) {
        Timber.tag(SYNC_LOG_TAG).d("Download finished")
        reader?.endArray()
        reader?.close()
    }

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
    }
}
