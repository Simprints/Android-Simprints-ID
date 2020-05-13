package com.simprints.id.data.db.person

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.*
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncProgress
import com.simprints.id.data.db.person.domain.Person.Companion.buildPersonFromCreationPayload
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordMovePayload
import com.simprints.id.data.db.person.domain.personevents.fromApiToDomain
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.services.scheduledSync.people.common.SYNC_LOG_TAG
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.SimJsonHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class PersonRepositoryDownSyncHelperImpl(val personLocalDataSource: PersonLocalDataSource,
                                         private val eventRemoteDataSource: EventRemoteDataSource,
                                         private val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                         val timeHelper: TimeHelper) : PersonRepositoryDownSyncHelper {

    private lateinit var downSyncOperation: PeopleDownSyncOperation

    @ExperimentalCoroutinesApi
    override suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                                     downSyncOperation: PeopleDownSyncOperation,
                                                     eventQuery: EventQuery): ReceiveChannel<PeopleDownSyncProgress> =
        scope.produce {

            this@PersonRepositoryDownSyncHelperImpl.downSyncOperation = downSyncOperation
            var reader: JsonReader? = null

            val bufferToSave = mutableListOf<ApiEvent>()

            try {
                val responseStream = getDownSyncStreamFromRemote(eventQuery)
                reader = setupJsonReaderFromResponse(responseStream)

                val channelFromNetwork = createChannelOfEvents(reader)
                while (!channelFromNetwork.isClosedForReceive) {
                    channelFromNetwork.poll()?.let {
                        bufferToSave.add(it)
                        if (bufferToSave.size > BATCH_SIZE_FOR_DOWNLOADING) {
                            this.send(PeopleDownSyncProgress(bufferToSave.size))
                            saveBatch(bufferToSave, downSyncOperation.moduleId)
                        }
                    }
                }

                saveBatch(bufferToSave, downSyncOperation.moduleId)
                updateDownSyncInfo(COMPLETE)
                finishDownload(reader)

            } catch (t: Throwable) {
                Timber.d(t)
                t.printStackTrace()
                saveBatch(bufferToSave, downSyncOperation.moduleId)
                finishDownload(reader)
                updateDownSyncInfo(FAILED)
                throw t
            }
        }

    private suspend fun getDownSyncStreamFromRemote(eventQuery: EventQuery) =
        eventRemoteDataSource.getStreaming(eventQuery)

    private fun setupJsonReaderFromResponse(responseStream: InputStream): JsonReader =
        JsonReader(InputStreamReader(responseStream) as Reader?)
            .also {
                it.beginArray()
            }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.createChannelOfEvents(reader: JsonReader) =
        produce<ApiEvent>(capacity = 5 * BATCH_SIZE_FOR_DOWNLOADING) {
            while (reader.hasNext()) {
                this.send(SimJsonHelper.gson.fromJson(reader, ApiEvent::class.java))
            }
        }

    private suspend fun saveBatch(batch: MutableList<ApiEvent>, moduleId: String?) {
        saveBatchAndUpdateDownSyncStatus(batch, moduleId)
        batch.clear()
    }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiEvent>, moduleId: String?) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople, moduleId)
        Timber.tag(SYNC_LOG_TAG).d("Saved batch(${batchOfPeople.size}) for $downSyncOperation")

        updateDownSyncInfo(RUNNING, batchOfPeople.lastOrNull(), Date())
    }

    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfEvents: List<ApiEvent>, moduleId: String?) {
        val batchOfPeopleToSaveInLocal =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordCreationPayload }.map {
                it.fromApiToDomain().payload as EnrolmentRecordCreationPayload
            }

        val eventRecordsToBeDeleted =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordDeletionPayload }.map {
                it.fromApiToDomain().payload as EnrolmentRecordDeletionPayload
            }

        val eventRecordsToMove =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordMovePayload }.map {
                it.fromApiToDomain().payload as EnrolmentRecordMovePayload
            }

        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
        deletePeopleBatchFromLocal(eventRecordsToBeDeleted)
        movePeopleBatchesInLocal(eventRecordsToMove, moduleId)
    }

    private suspend fun savePeopleBatchInLocal(batchOfEventsToSaveInLocal: List<EnrolmentRecordCreationPayload>) {
        if (batchOfEventsToSaveInLocal.isNotEmpty()) {
            personLocalDataSource.insertOrUpdate(batchOfEventsToSaveInLocal.map { buildPersonFromCreationPayload(it) })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(eventRecordsToBeDeleted: List<EnrolmentRecordDeletionPayload>) {
        if (eventRecordsToBeDeleted.isNotEmpty()) {
            personLocalDataSource.delete(buildQueryForPeopleById(eventRecordsToBeDeleted))
        }
    }

    private suspend fun movePeopleBatchesInLocal(eventRecordsToMove: List<EnrolmentRecordMovePayload>, moduleId: String?) {
        if (eventRecordsToMove.isNotEmpty()) {
            if (moduleId != null) {
                deletePeopleBatchFromLocal(eventRecordsToMove.map { it.enrolmentRecordDeletion }.filter { it.moduleId == moduleId })
                savePeopleBatchInLocal(eventRecordsToMove.map { it.enrolmentRecordCreation }.filter { it.moduleId == moduleId })
            } else {
                deletePeopleBatchFromLocal(eventRecordsToMove.map { it.enrolmentRecordDeletion })
                savePeopleBatchInLocal(eventRecordsToMove.map { it.enrolmentRecordCreation })
            }
        }
    }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<EnrolmentRecordDeletionPayload>) =
        batchOfPeopleToBeDeleted.map {
            PersonLocalDataSource.Query(personId = it.subjectId)
        }

    private suspend fun updateDownSyncInfo(state: PeopleDownSyncOperationResult.DownSyncState,
                                           event: ApiEvent? = null,
                                           lastSyncTime: Date? = null) {
        var newResultInfo = downSyncOperation.lastResult?.copy(state = state)
            ?: PeopleDownSyncOperationResult(state, null, null)

        if (event != null) {
            newResultInfo = event.let {
                newResultInfo.copy(
                    lastEventId = event.id)
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
