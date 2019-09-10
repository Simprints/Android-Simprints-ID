package com.simprints.id.data.db.person

import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Single

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource) : PersonRepository {

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
}
