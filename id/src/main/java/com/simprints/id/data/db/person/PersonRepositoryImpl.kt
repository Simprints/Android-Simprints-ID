package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.LOCAL
import com.simprints.id.data.db.PersonFetchResult.PersonSource.REMOTE
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.PeopleOperationsParams
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncstatus.downsyncinfo.getStatusId
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PersonRepositoryImpl(val personRemoteDataSource: PersonRemoteDataSource,
                           val personLocalDataSource: PersonLocalDataSource,
                           private val peopleUpSyncMaster: PeopleUpSyncMaster,
                           private val downSyncDao: DownSyncDao) :
    PersonRepository,
    PersonLocalDataSource by personLocalDataSource,
    PersonRemoteDataSource by personRemoteDataSource {

    override fun countToDownSync(syncScope: SyncScope): Single<List<PeopleCount>> =
        personRemoteDataSource.getDownSyncPeopleCount(buildPeopleOperationsParams(syncScope))

    private fun buildPeopleOperationsParams(syncScope: SyncScope) = when (syncScope.group) {
        GROUP.GLOBAL -> buildPeopleOperationsParamsForProjectSync(syncScope)
        GROUP.USER -> buildPeopleOperationsParamsForUserSync(syncScope)
        GROUP.MODULE -> buildPeopleOperationsParamsForModuleSync(syncScope)
    }

    private fun buildPeopleOperationsParamsForProjectSync(syncScope: SyncScope) =
        with (syncScope.toSubSyncScopes().first()) {
            listOf(PeopleOperationsParams(this, getLastKnownPatientId(projectId, userId, moduleId),
                getLastKnownPatientUpdatedAt(projectId, userId, moduleId)))
        }

    private fun buildPeopleOperationsParamsForUserSync(syncScope: SyncScope) =
        with (syncScope.toSubSyncScopes().first()) {
            listOf(PeopleOperationsParams(this, getLastKnownPatientId(projectId, userId, moduleId),
                getLastKnownPatientUpdatedAt(projectId, userId, moduleId)))
        }

    private fun buildPeopleOperationsParamsForModuleSync(syncScope: SyncScope) =
        syncScope.toSubSyncScopes().map {
            PeopleOperationsParams(it, getLastKnownPatientId(it.projectId, it.userId, it.moduleId),
                getLastKnownPatientUpdatedAt(it.projectId, it.userId, it.moduleId))
        }

    private fun getLastKnownPatientId(projectId: String, userId: String?, moduleId: String?): String? =
        downSyncDao.getDownSyncStatusForId(getDownSyncId(projectId, userId, moduleId))?.lastPatientId

    private fun getLastKnownPatientUpdatedAt(projectId: String, userId: String?, moduleId: String?): Long? =
        downSyncDao.getDownSyncStatusForId(getDownSyncId(projectId, userId, moduleId))?.lastPatientUpdatedAt

    private fun getDownSyncId(projectId: String, userId: String?, moduleId: String?) =
        downSyncDao.getStatusId(projectId, userId, moduleId)

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

    override suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult =
        try {
            val person = personLocalDataSource.load(PersonLocalDataSource.Query(patientId = patientId)).first()
            PersonFetchResult(person, LOCAL)
        } catch (t: Throwable) {
            tryToFetchPersonFromRemote(projectId, patientId)
        }

    private suspend fun tryToFetchPersonFromRemote(projectId: String, patientId: String): PersonFetchResult =
        suspendCancellableCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                personRemoteDataSource.downloadPerson(patientId = patientId, projectId = projectId)
                    .subscribeBy(
                    onSuccess = { cont.resume(PersonFetchResult(it, REMOTE)) },
                    onError = { cont.resumeWithException(it) }
                )
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
