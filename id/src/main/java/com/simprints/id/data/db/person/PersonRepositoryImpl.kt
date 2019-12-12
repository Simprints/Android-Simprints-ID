package com.simprints.id.data.db.person

import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.domain.DownSyncScope
import com.simprints.id.data.db.down_sync_info.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           val downSyncScopeRepository: DownSyncScopeRepository,
                           private val peopleUpSyncMaster: PeopleUpSyncMaster) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource {

    override suspend fun countToDownSync(downSyncScope: DownSyncScope): List<PeopleCount> =
        personRemoteDataSource.getDownSyncPeopleCount(downSyncScope.projectId, downSyncScopeRepository.getDownSyncOperations(downSyncScope))
            .subscribeOn(Schedulers.io())
            .blockingGet()

    override suspend fun localCountForSyncScope(downSyncScope: DownSyncScope): List<PeopleCount> =
        downSyncScopeRepository.getDownSyncOperations(downSyncScope).map {
            PeopleCount(
                personLocalDataSource.count(PersonLocalDataSource.Query(
                    projectId = it.projectId,
                    userId = it.userId,
                    moduleId = it.moduleId)), 0, 0)
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

    private fun savePersonInLocal(person: Person) {
        CoroutineScope(Dispatchers.IO).launch {
            personLocalDataSource.insertOrUpdate(listOf(person))
        }
    }

    override suspend fun saveAndUpload(person: Person) {
        personLocalDataSource.insertOrUpdate(listOf(person.apply { toSync = true }))
        scheduleUpsync(person.projectId, person.userId)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun scheduleUpsync(projectId: String, userId: String) {
        peopleUpSyncMaster.schedule(projectId/*, userId*/)
    }
}
