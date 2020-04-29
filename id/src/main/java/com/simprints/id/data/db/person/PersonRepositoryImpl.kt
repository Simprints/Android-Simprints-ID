package com.simprints.id.data.db.person

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.*
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.person.PersonRepositoryDownSyncHelper.Companion.buildPersonFromCreationPayload
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.domain.personevents.Event
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType.*
import com.simprints.id.data.db.person.domain.personevents.fromApiToDomain
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.sync.NoModulesSelectedForModuleSyncException
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.tools.json.SimJsonHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class PersonRepositoryImpl(private val eventRemoteDataSource: EventRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                           private val peopleUpSyncExecutor: PeopleUpSyncExecutor,
                           private val personRepositoryUpSyncHelper: PersonRepositoryUpSyncHelper,
                           private val personRepositoryDownSyncHelper: PersonRepositoryDownSyncHelper) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    EventRemoteDataSource by eventRemoteDataSource {

    override suspend fun countToDownSync(peopleDownSyncScope: PeopleDownSyncScope): PeopleCount {
        val eventCounts = eventRemoteDataSource.count(buildEventQuery(peopleDownSyncScope))
        return buildPeopleCountFromEventCounts(eventCounts)
    }

    override suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult =
        try {
            val person = personLocalDataSource.load(PersonLocalDataSource.Query(personId = patientId)).first()
            PersonFetchResult(person, LOCAL)
        } catch (t: Throwable) {
            tryToFetchPersonFromRemote(projectId, patientId).also { personFetchResult ->
                personFetchResult.person?.let { savePersonInLocal(it) }
            }
        }

    private suspend fun tryToFetchPersonFromRemote(projectId: String, patientId: String): PersonFetchResult {
        val eventQuery = buildEventQueryForPersonFetch(projectId, patientId)
        val inputStream = eventRemoteDataSource.getStreaming(eventQuery)
        val reader = setupJsonReaderFromResponseStream(inputStream)
        return if (reader.hasNext()) {
            val apiEvent: ApiEvent = SimJsonHelper.gson.fromJson(reader, ApiEvent::class.java)
            createPersonFetchResultFromEvent(apiEvent.fromApiToDomain())
        } else {
            PersonFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        }
    }

    private fun setupJsonReaderFromResponseStream(responseStream: InputStream): JsonReader =
        JsonReader(InputStreamReader(responseStream) as Reader?)
            .also {
                it.beginArray()
            }

    private fun createPersonFetchResultFromEvent(event: Event): PersonFetchResult {
        val person = buildPersonFromCreationPayload(event.payload as EnrolmentRecordCreationPayload)
        return PersonFetchResult(person, REMOTE)
    }

    private suspend fun savePersonInLocal(person: Person) = personLocalDataSource.insertOrUpdate(listOf(person))

    override suspend fun saveAndUpload(person: Person) {
        personLocalDataSource.insertOrUpdate(listOf(person.apply { toSync = true }))
        peopleUpSyncExecutor.sync()
    }

    private fun buildPeopleCountFromEventCounts(eventCounts: List<EventCount>): PeopleCount {
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

        return PeopleCount(created, deleted, updated)
    }

    override suspend fun performUploadWithProgress(scope: CoroutineScope) =
        personRepositoryUpSyncHelper.executeUploadWithProgress(scope)

    override suspend fun performDownloadWithProgress(scope: CoroutineScope,
                                                     peopleDownSyncOperation: PeopleDownSyncOperation): ReceiveChannel<Int> =
        personRepositoryDownSyncHelper.performDownSyncWithProgress(scope, peopleDownSyncOperation,
            buildEventQuery(downSyncScopeRepository.getDownSyncScope()))

    private fun buildEventQuery(peopleDownSyncScope: PeopleDownSyncScope) =
        with(peopleDownSyncScope) {
            when (this) {
                is ProjectSyncScope -> {
                    EventQuery(projectId, modes = modes,
                        types = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_DELETION, ENROLMENT_RECORD_MOVE))
                }
                is UserSyncScope -> {
                    EventQuery(projectId, userId = userId, modes = modes,
                        types = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_DELETION, ENROLMENT_RECORD_MOVE))
                }
                is ModuleSyncScope -> {
                    if (modules.isEmpty()) {
                        throw NoModulesSelectedForModuleSyncException()
                    }
                    EventQuery(projectId, moduleIds = modules, modes = modes,
                        types = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_DELETION, ENROLMENT_RECORD_MOVE))
                }
            }
        }

    private fun buildEventQueryForPersonFetch(projectId: String, patientId: String) = EventQuery(
        projectId = projectId,
        subjectId = patientId,
        modes = listOf(Modes.FINGERPRINT),
        types = listOf(ENROLMENT_RECORD_CREATION)
    )
}
