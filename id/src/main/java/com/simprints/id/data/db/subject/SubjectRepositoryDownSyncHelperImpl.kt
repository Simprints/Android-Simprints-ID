package com.simprints.id.data.db.subject

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.models.ApiEvent
import com.simprints.id.data.db.subject.domain.Subject.Companion.buildSubjectFromCreationPayload
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult.DownSyncState.*
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncProgress
import com.simprints.id.data.db.event.remote.ApiEventQuery
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
                                                     eventQuery: ApiEventQuery): ReceiveChannel<SubjectsDownSyncProgress> =
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
                            saveBatch(bufferToSave, downSyncOperation.moduleId, downSyncOperation.attendantId)
                        }
                    }
                }

                saveBatch(bufferToSave, downSyncOperation.moduleId, downSyncOperation.attendantId)
                updateDownSyncInfo(COMPLETE)
                finishDownload(reader)

            } catch (t: Throwable) {
                Timber.d(t)
                t.printStackTrace()
                saveBatch(bufferToSave, downSyncOperation.moduleId, downSyncOperation.attendantId)
                finishDownload(reader)
                updateDownSyncInfo(FAILED)
                throw t
            }
        }

    private suspend fun getDownSyncStreamFromRemote(eventQuery: ApiEventQuery) =
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

    private suspend fun saveBatch(batch: MutableList<ApiEvent>, moduleId: String?, attendantId: String?) {
        saveBatchAndUpdateDownSyncStatus(batch, moduleId, attendantId)
        batch.clear()
    }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiEvent>,
                                                         moduleId: String?, attendantId: String?) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople, moduleId, attendantId)
        Timber.tag(SYNC_LOG_TAG).d("Saved batch(${batchOfPeople.size}) for $downSyncOperation")

        updateDownSyncInfo(RUNNING, batchOfPeople.lastOrNull(), Date())
    }

    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfEvents: List<ApiEvent>,
                                                           moduleId: String?, attendantId: String?) {

        //STOPSHIP
//        val batchOfPeopleToSaveInLocal =
//            batchOfEvents.filter { it.payload is ApiEnrolmentRecordCreationPayload }.mapNotNull { apiEvent ->
//                apiEvent.fromApiToDomainOrNullIfNoBiometricReferences()?.let {
//                    it.payload as EnrolmentRecordCreationPayload
//                }
//            }
//
//        val eventRecordsToBeDeleted =
//            batchOfEvents.filter { it.payload is ApiEnrolmentRecordDeletionPayload }.mapNotNull { apiEvent ->
//                apiEvent.fromApiToDomainOrNullIfNoBiometricReferences()?.let {
//                    it.payload as EnrolmentRecordDeletionPayload
//                }
//            }
//
//        val eventRecordsToMove =
//            batchOfEvents.filter { it.payload is ApiEnrolmentRecordMovePayload }.mapNotNull { apiEvent ->
//                apiEvent.fromApiToDomainOrNullIfNoBiometricReferences()?.let {
//                    it.payload as EnrolmentRecordMovePayload
//                }
//            }
//
//        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
//        deletePeopleBatchFromLocal(eventRecordsToBeDeleted)
//        movePeopleBatchesInLocal(eventRecordsToMove, moduleId, attendantId)
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

    private suspend fun movePeopleBatchesInLocal(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                 moduleId: String?, attendantId: String?) {
//        if (eventRecordsToMove.isNotEmpty()) {
//            when {
//                moduleId != null -> {
//                    deletePeopleBatchFromLocal(getRecordsToBeDeletedFilteredByModule(eventRecordsToMove, moduleId))
//                    savePeopleBatchInLocal(getRecordsToBeSavedFilteredByModule(eventRecordsToMove, moduleId))
//                }
//                attendantId != null -> {
//                    deletePeopleBatchFromLocal(getRecordsToBeDeletedFilteredByUser(eventRecordsToMove, attendantId))
//                    savePeopleBatchInLocal(getRecordsToBeSavedFilteredByUser(eventRecordsToMove, attendantId))
//                }
//                else -> {
//                    deletePeopleBatchFromLocal(eventRecordsToMove.map { it.enrolmentRecordDeletion })
//                    savePeopleBatchInLocal(eventRecordsToMove.mapNotNull { it.enrolmentRecordCreation })
//                }
//            }
//        }
    }

    private fun getRecordsToBeDeletedFilteredByModule(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                      moduleId: String) =
            eventRecordsToMove.map { it.enrolmentRecordDeletion }.filter { it.moduleId == moduleId }

    private fun getRecordsToBeSavedFilteredByModule(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                    moduleId: String) =
            eventRecordsToMove.mapNotNull { it.enrolmentRecordCreation }.filter { it.moduleId == moduleId }

    private fun getRecordsToBeDeletedFilteredByUser(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                    attendantId: String) =
        eventRecordsToMove.map { it.enrolmentRecordDeletion }.filter { it.attendantId == attendantId }

    private fun getRecordsToBeSavedFilteredByUser(eventRecordsToMove: List<EnrolmentRecordMovePayload>,
                                                  attendantId: String) =
        eventRecordsToMove.mapNotNull { it.enrolmentRecordCreation }.filter { it.attendantId == attendantId }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<EnrolmentRecordDeletionPayload>) =
        batchOfPeopleToBeDeleted.map {
            SubjectLocalDataSource.Query(subjectId = it.subjectId)
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
