package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.people_sync.down.domain.toEventQuery
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import kotlinx.coroutines.flow.first

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val eventRemoteDataSource: EventRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                           private val peopleUpSyncExecutor: PeopleUpSyncExecutor) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource,
    EventRemoteDataSource by eventRemoteDataSource {

    override suspend fun countToDownSync(peopleDownSyncScope: PeopleDownSyncScope): PeopleCount {
        val eventCounts = eventRemoteDataSource.count(peopleDownSyncScope.toEventQuery())
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
                EventType.EnrolmentRecordCreation -> created += it.count
                EventType.EnrolmentRecordDeletion -> deleted += it.count
                EventType.EnrolmentRecordMove -> updated += it.count
            }
        }

        return PeopleCount(created, deleted, updated)
    }
}
