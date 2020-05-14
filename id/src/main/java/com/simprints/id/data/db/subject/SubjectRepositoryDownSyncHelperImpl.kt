package com.simprints.id.data.db.subject

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.subject.domain.Subject.Companion.buildSubjectFromCreationPayload
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordMovePayload
import com.simprints.id.data.db.subject.domain.subjectevents.fromApiToDomain
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.remote.EventRemoteDataSource
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvent
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult.DownSyncState.*
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncProgress
import com.simprints.id.services.scheduledSync.subjects.common.SYNC_LOG_TAG
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

class SubjectRepositoryDownSyncHelperImpl(val subjectLocalDataSource: SubjectLocalDataSource,
                                          private val eventRemoteDataSource: EventRemoteDataSource,
                                          private val downSyncScopeRepository: SubjectsDownSyncScopeRepository,
                                          val timeHelper: TimeHelper) : SubjectRepositoryDownSyncHelper {

    private lateinit var downSyncOperation: SubjectsDownSyncOperation

    @ExperimentalCoroutinesApi
    override suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                                     downSyncOperation: SubjectsDownSyncOperation,
                                                     eventQuery: EventQuery): ReceiveChannel<SubjectsDownSyncProgress> =
        scope.produce {

            this@SubjectRepositoryDownSyncHelperImpl.downSyncOperation = downSyncOperation
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
                            this.send(SubjectsDownSyncProgress(bufferToSave.size))
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
            subjectLocalDataSource.insertOrUpdate(batchOfEventsToSaveInLocal.map { buildSubjectFromCreationPayload(it) })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(eventRecordsToBeDeleted: List<EnrolmentRecordDeletionPayload>) {
        if (eventRecordsToBeDeleted.isNotEmpty()) {
            subjectLocalDataSource.delete(buildQueryForPeopleById(eventRecordsToBeDeleted))
        }
    }

    private suspend fun movePeopleBatchesInLocal(eventRecordsToMove: List<EnrolmentRecordMovePayload>, moduleId: String?) {
        if (eventRecordsToMove.isNotEmpty()) {
            deletePeopleBatchFromLocal(getRecordsToBeDeletedFilteredByModuleIfNotNull(eventRecordsToMove, moduleId))
            savePeopleBatchInLocal(getRecordsToBeSavedFilteredByModuleIfNotNull(eventRecordsToMove, moduleId))
        }
    }

    private fun getRecordsToBeDeletedFilteredByModuleIfNotNull(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                               moduleId: String?) =
        if (moduleId != null) {
            eventRecordsToMove.map { it.enrolmentRecordDeletion }.filter { it.moduleId == moduleId }
        } else {
            eventRecordsToMove.map { it.enrolmentRecordDeletion }
        }

    private fun getRecordsToBeSavedFilteredByModuleIfNotNull(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                             moduleId: String?) =
        if (moduleId != null) {
            eventRecordsToMove.map { it.enrolmentRecordCreation }.filter { it.moduleId == moduleId }
        } else {
            eventRecordsToMove.map { it.enrolmentRecordCreation }
        }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<EnrolmentRecordDeletionPayload>) =
        batchOfPeopleToBeDeleted.map {
            SubjectLocalDataSource.Query(personId = it.subjectId)
        }

    private suspend fun updateDownSyncInfo(state: SubjectsDownSyncOperationResult.DownSyncState,
                                           event: ApiEvent? = null,
                                           lastSyncTime: Date? = null) {
        var newResultInfo = downSyncOperation.lastResult?.copy(state = state)
            ?: SubjectsDownSyncOperationResult(state, null, null)

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
