package com.simprints.id.data.db.person

import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncManager
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                           private val peopleUpSyncManager: PeopleUpSyncManager) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource {

    override suspend fun countToDownSync(peopleDownSyncScope: PeopleDownSyncScope): List<PeopleCount> =
        personRemoteDataSource.getDownSyncPeopleCount(peopleDownSyncScope.projectId, downSyncScopeRepository.getDownSyncOperations(peopleDownSyncScope))
            .subscribeOn(Schedulers.io())
            .blockingGet()


    override suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult =
        try {
            val person = personLocalDataSource.load(PersonLocalDataSource.Query(personId = patientId)).first()
            PersonFetchResult(person, LOCAL)
        } catch (t: Throwable) {
            tryToFetchPersonFromRemote(projectId, patientId).also { personFetchResult ->
                personFetchResult.person?.let { savePersonInLocal(it) }
            }
        }

    private suspend fun tryToFetchPersonFromRemote(projectId: String, patientId: String): PersonFetchResult =
        suspendCancellableCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                personRemoteDataSource.downloadPerson(patientId = patientId, projectId = projectId)
                    .subscribeBy(
                        onSuccess = { cont.resumeSafely(PersonFetchResult(it, REMOTE)) },
                        onError = { cont.resumeWithExceptionSafely(it) }
                    )
            }
        }

    private suspend fun savePersonInLocal(person: Person) = personLocalDataSource.insertOrUpdate(listOf(person))

    override suspend fun saveAndUpload(person: Person) {
        personLocalDataSource.insertOrUpdate(listOf(person.apply { toSync = true }))
        peopleUpSyncManager.sync()
    }
}
