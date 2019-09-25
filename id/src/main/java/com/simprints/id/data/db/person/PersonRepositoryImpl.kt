package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import io.reactivex.Single
import kotlinx.coroutines.flow.first

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           private val peopleUpSyncMaster: PeopleUpSyncMaster) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource {

    override fun countToDownSync(syncScope: SyncScope): Single<List<PeopleCount>> =
        personRemoteDataSource.getDownSyncPeopleCount(syncScope).flatMap { peopleCountInRemote ->
            localCountForSyncScope(syncScope).map { peopleCountsInLocal ->
                calculateDifferenceBetweenRemoteAndLocal(peopleCountInRemote, peopleCountsInLocal)
            }
        }

    override fun localCountForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>> =
        Single.just(
            syncScope.toSubSyncScopes().map {
                PeopleCount(it.projectId,
                    it.userId,
                    it.moduleId,
                    syncScope.modes,
                    personLocalDataSource.count(PersonLocalDataSource.Query(
                        projectId = it.projectId,
                        userId = it.userId,
                        moduleId = it.moduleId)))
            }
        )


    private fun calculateDifferenceBetweenRemoteAndLocal(peopleCountInRemote: List<PeopleCount>,
                                                         peopleCountsInLocal: List<PeopleCount>): List<PeopleCount> =
        peopleCountInRemote.map { remotePeopleCount ->
            val localCount = peopleCountsInLocal.find {
                it.projectId == remotePeopleCount.projectId &&
                    it.userId == remotePeopleCount.userId &&
                    it.moduleId == remotePeopleCount.moduleId &&
                    it.modes?.joinToString() == remotePeopleCount.modes?.joinToString()
            }?.count ?: 0

            remotePeopleCount.copy(count = remotePeopleCount.count - localCount)
        }

    override suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult =
        try {
            val person = personLocalDataSource.load(PersonLocalDataSource.Query(patientId = patientId)).first()
            PersonFetchResult(person, LOCAL)
        } catch (t: Throwable) {
            tryToFetchPersonFromRemote(projectId, patientId)
        }

    private fun tryToFetchPersonFromRemote(projectId: String, patientId: String) = try {
        val person = personRemoteDataSource.downloadPerson(projectId, patientId).blockingGet()
        PersonFetchResult(person, REMOTE)
    } catch (t: Throwable) {
        throw t
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
