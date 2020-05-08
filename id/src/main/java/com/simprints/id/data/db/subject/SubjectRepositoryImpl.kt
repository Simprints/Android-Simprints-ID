package com.simprints.id.data.db.subject

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.Subject.Companion.buildSubjectFromCreationPayload
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.domain.subjectevents.EventPayloadType.*
import com.simprints.id.data.db.subject.domain.subjectevents.fromApiToDomain
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.remote.EventRemoteDataSource
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvent
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncProgress
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.subjects.up.controllers.SubjectsUpSyncExecutor
import com.simprints.id.tools.json.SimJsonHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class SubjectRepositoryImpl(private val eventRemoteDataSource: EventRemoteDataSource,
                           val subjectLocalDataSource: SubjectLocalDataSource,
                           val downSyncScopeRepository: SubjectsDownSyncScopeRepository,
                           private val peopleUpSyncExecutor: SubjectsUpSyncExecutor,
                           private val personRepositoryUpSyncHelper: SubjectRepositoryUpSyncHelper,
                           private val personRepositoryDownSyncHelper: SubjectRepositoryDownSyncHelper) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource,
    EventRemoteDataSource by eventRemoteDataSource {

    override suspend fun countToDownSync(subjectsDownSyncScope: SubjectsDownSyncScope): SubjectsCount {
        val downSyncOperations = downSyncScopeRepository.getDownSyncOperations(subjectsDownSyncScope)
        val peopleCounts = makeRequestAndBuildPeopleCountList(downSyncOperations)
        return combinePeopleCounts(peopleCounts)
    }

    private suspend fun makeRequestAndBuildPeopleCountList(downSyncOperations: List<SubjectsDownSyncOperation>) =
        downSyncOperations.map {
            buildPeopleCountFromEventCounts(
                eventRemoteDataSource.count(buildEventQuery(it))
            )
        }

    private fun combinePeopleCounts(subjectsCounts: List<SubjectsCount>) = with(subjectsCounts) {
        SubjectsCount(
            sumBy { it.created },
            sumBy { it.deleted },
            sumBy { it.updated }
        )
    }

    override suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): SubjectFetchResult =
        try {
            val person = subjectLocalDataSource.load(SubjectLocalDataSource.Query(personId = patientId)).first()
            SubjectFetchResult(person, LOCAL)
        } catch (t: Throwable) {
            tryToFetchPersonFromRemote(projectId, patientId).also { subjectFetchResult ->
                subjectFetchResult.subject?.let { savePersonInLocal(it) }
            }
        }

    private suspend fun tryToFetchPersonFromRemote(projectId: String, patientId: String): SubjectFetchResult {
        val eventQuery = buildEventQueryForPersonFetch(projectId, patientId)
        val inputStream = eventRemoteDataSource.getStreaming(eventQuery)
        val reader = setupJsonReaderFromResponseStream(inputStream)
        return if (reader.hasNext()) {
            val apiEvent: ApiEvent = SimJsonHelper.gson.fromJson(reader, ApiEvent::class.java)
            val event = apiEvent.fromApiToDomain()
            val subject = buildSubjectFromCreationPayload(event.payload as EnrolmentRecordCreationPayload)
            SubjectFetchResult(subject, REMOTE)
        } else {
            SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        }
    }

    private fun setupJsonReaderFromResponseStream(responseStream: InputStream): JsonReader =
        JsonReader(InputStreamReader(responseStream) as Reader?)
            .also {
                it.beginArray()
            }

    private suspend fun savePersonInLocal(subject: Subject) = subjectLocalDataSource.insertOrUpdate(listOf(subject))

    override suspend fun saveAndUpload(subject: Subject) {
        subjectLocalDataSource.insertOrUpdate(listOf(subject.apply { toSync = true }))
        peopleUpSyncExecutor.sync()
    }

    private fun buildPeopleCountFromEventCounts(eventCounts: List<EventCount>): SubjectsCount {
        var created = 0
        var deleted = 0
        var updated = 0
        eventCounts.forEach {
            when (it.type) {
                ENROLMENT_RECORD_CREATION -> created += it.count
                ENROLMENT_RECORD_DELETION -> deleted += it.count
                ENROLMENT_RECORD_MOVE -> updated += it.count
            }
        }

        return SubjectsCount(created, deleted, updated)
    }

    override suspend fun performUploadWithProgress(scope: CoroutineScope) =
        personRepositoryUpSyncHelper.executeUploadWithProgress(scope)

    override suspend fun performDownloadWithProgress(scope: CoroutineScope,
                                                     peopleDownSyncOperation: SubjectsDownSyncOperation): ReceiveChannel<SubjectsDownSyncProgress> =
        personRepositoryDownSyncHelper.performDownSyncWithProgress(scope, peopleDownSyncOperation,
            buildEventQuery(peopleDownSyncOperation))

    private fun buildEventQuery(peopleDownSyncOperation: SubjectsDownSyncOperation) =
        with(peopleDownSyncOperation) {
            EventQuery(
                projectId = projectId,
                userId = userId,
                moduleIds = moduleId?.let { listOf(it) },
                lastEventId = lastResult?.lastEventId,
                modes = modes,
                types = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_DELETION, ENROLMENT_RECORD_MOVE)
            )
        }

    private fun buildEventQueryForPersonFetch(projectId: String, patientId: String) = EventQuery(
        projectId = projectId,
        subjectId = patientId,
        modes = listOf(Modes.FINGERPRINT),
        types = listOf(ENROLMENT_RECORD_CREATION)
    )
}
