package com.simprints.id.data.db.person

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.*
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.services.scheduledSync.people.common.SYNC_LOG_TAG
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.SimJsonHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import okhttp3.ResponseBody
import timber.log.Timber
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
                            this.send(bufferToSave.size)
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

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.createPeopleChannelFromJsonReader(reader: JsonReader) =
        produce<ApiEvent>(capacity = 5 * BATCH_SIZE_FOR_DOWNLOADING) {
            while (reader.hasNext()) {
                this.send(SimJsonHelper.gson.fromJson(reader, ApiEvent::class.java))
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
            }.map { it.payload as EnrolmentRecordCreationPayload }

        val eventRecordsToBeDeleted =
            batchOfEvents.filter { it.payload is ApiEnrolmentRecordDeletionPayload }.map {
                it.fromApiToDomain()
            }.map { it.payload as EnrolmentRecordDeletionPayload }

        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
        deletePeopleBatchFromLocal(eventRecordsToBeDeleted)
    }

    private suspend fun savePeopleBatchInLocal(batchOfPeopleToSaveInLocal: List<EnrolmentRecordCreationPayload>) {
        if (batchOfPeopleToSaveInLocal.isNotEmpty()) {
            personLocalDataSource.insertOrUpdate(batchOfPeopleToSaveInLocal.map { buildPersonFromCreationPayload(it) })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(eventRecordsToBeDeleted: List<EnrolmentRecordDeletionPayload>) {
        if (eventRecordsToBeDeleted.isNotEmpty()) {
            personLocalDataSource.delete(buildQueryForPeopleById(eventRecordsToBeDeleted))
        }
    }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<EnrolmentRecordDeletionPayload>) =
        batchOfPeopleToBeDeleted.map {
            PersonLocalDataSource.Query(personId = it.subjectId)
        }

    private fun buildPersonFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
        Person(
            patientId = subjectId,
            projectId = projectId,
            userId = attendantId,
            moduleId = moduleId,
            toSync = false,
            fingerprintSamples = biometricReferences.filterIsInstance(FingerprintReference::class.java)
                .firstOrNull()?.templates?.map { buildFingerprintSample(it) } ?: emptyList(),
            faceSamples = biometricReferences.filterIsInstance(FaceReference::class.java)
                .firstOrNull()?.templates?.map { buildFaceSample(it) } ?: emptyList()
        )
    }

    private fun buildFingerprintSample(template: FingerprintTemplate) =
        FingerprintSample(
            template.finger.fromEventToPerson(),
            EncodingUtils.base64ToBytes(template.template),
            template.quality
        )

    private fun buildFaceSample(template: FaceTemplate) =
        FaceSample(EncodingUtils.base64ToBytes(template.template))

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
