package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType.*
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.exceptions.safe.sync.NoModulesSelectedForModuleSyncException
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           private val eventRemoteDataSource: EventRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                           private val peopleUpSyncExecutor: PeopleUpSyncExecutor,
                           val personRepositoryUpSyncHelper: PersonRepositoryUpSyncHelper) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource,
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
        val person = personRemoteDataSource.downloadPerson(patientId = patientId, projectId = projectId)
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
                EventType.ENROLMENT_RECORD_CREATION -> created += it.count
                EventType.ENROLMENT_RECORD_DELETION -> deleted += it.count
                EventType.ENROLMENT_RECORD_MOVE -> updated += it.count
            }
        }

        return PeopleCount(created, deleted, updated)
    }

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

    override suspend fun performUploadWithProgress(scope: CoroutineScope) =
        personRepositoryUpSyncHelper.executeUploadWithProgress(scope)
}
