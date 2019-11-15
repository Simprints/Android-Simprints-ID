package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.PeopleOperationsCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.fromDomainToPostApi
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.*
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.toDomainPeopleCount
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.unexpected.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.tools.extensions.handleResponse
import com.simprints.id.tools.extensions.handleResult
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException


open class PersonRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager) : PersonRemoteDataSource {

    override fun downloadPerson(patientId: String, projectId: String): Single<Person> =
        getPeopleApiClient().flatMap { peopleRemoteInterface ->
            peopleRemoteInterface.requestPerson(patientId, projectId)
                .retry(::retryCriteria)
                .handleResponse {
                    when (it.code()) {
                        404 -> throw DownloadingAPersonWhoDoesntExistOnServerException()
                        in 500..599 -> throw SimprintsInternalServerException()
                        else -> throw it
                    }
                }
                .map(ApiGetPerson::fromGetApiToDomain)
        }

    override fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable =
        getPeopleApiClient().flatMapCompletable {
            it.uploadPeople(projectId, hashMapOf("patients" to patientsToUpload.map(Person::fromDomainToPostApi)))
                .retry(::retryCriteria)
                .trace("uploadPatientBatch")
                .handleResult(::defaultResponseErrorHandling)
                .trace("uploadPatientBatch")
        }

    override fun getDownSyncPeopleCount(countScopes: List<PeopleOperationsCount>): Single<List<PeopleCount>> {

        return getPeopleApiClient().flatMap { peopleRemoteInterface ->
            with(countScopes.first().subSyncScope) {
                peopleRemoteInterface.requestPeopleOperations(
                    projectId,
                    buildApiPeopleOperations(countScopes))
                    .retry(::retryCriteria)
                    .handleResponse(::defaultResponseErrorHandling)
                    .trace("countRequest")
                    .map { response ->
                        response.toDomainPeopleCount(
                            projectId,
                            userId,
                            countScopes.map { it.subSyncScope.moduleId },
                            listOf(Modes.FINGERPRINT))
                    }
            }
        }
    }

    private fun buildApiPeopleOperations(countScopes: List<PeopleOperationsCount>) =
        ApiPeopleOperations(buildGroups(countScopes))

    private fun buildGroups(countScopes: List<PeopleOperationsCount>) =
        with(countScopes.first()) {
            when (subSyncScope.group) {
                GROUP.GLOBAL -> buildGroupForProjectSync(lastKnownPatientId, lastKnownPatientUpdatedAt)
                GROUP.USER -> buildGroupForUserSync(subSyncScope.userId
                    ?: "", lastKnownPatientId, lastKnownPatientUpdatedAt)
                GROUP.MODULE -> buildGroupForModuleSync(countScopes)
            }
        }

    private fun buildGroupForModuleSync(countScopes: List<PeopleOperationsCount>) =
        countScopes.map { peopleOpsCount ->
            ApiPeopleOperationGroup(
                buildApiLastKnownPatient(peopleOpsCount.lastKnownPatientId, peopleOpsCount.lastKnownPatientUpdatedAt),
                listOf(ApiPeopleOperationWhereLabel(WhereLabelKey.MODULE.key, peopleOpsCount.subSyncScope.moduleId
                    ?: ""), buildWhereLabelForFingerprintMode()))
        }

    private fun buildGroupForUserSync(userId: String,
                                      lastKnownPatientId: String?,
                                      lastKnownPatientUpdatedAt: Long?) =
        listOf(
            ApiPeopleOperationGroup(
                buildApiLastKnownPatient(lastKnownPatientId, lastKnownPatientUpdatedAt),
                listOf(ApiPeopleOperationWhereLabel(WhereLabelKey.USER.key, userId), buildWhereLabelForFingerprintMode())
            )
        )

    private fun buildGroupForProjectSync(lastKnownPatientId: String?, lastKnownPatientUpdatedAt: Long?) = listOf(
        ApiPeopleOperationGroup(buildApiLastKnownPatient(lastKnownPatientId, lastKnownPatientUpdatedAt),
            listOf(buildWhereLabelForFingerprintMode()))
    )

    private fun buildApiLastKnownPatient(lastKnownPatientId: String?, lastKnownPatientUpdatedAt: Long?) =
        lastKnownPatientId?.let { lastKnownId ->
            lastKnownPatientUpdatedAt?.let { lastKnownUpdatedAt ->
                ApiLastKnownPatient(lastKnownId, lastKnownUpdatedAt)
            }
        }

    private fun buildWhereLabelForFingerprintMode() =
        ApiPeopleOperationWhereLabel(WhereLabelKey.MODE.key, PipeSeparatorWrapperForURLListParam(ApiModes.FINGERPRINT).toString())

    override fun getPeopleApiClient(): Single<PeopleRemoteInterface> =
        remoteDbManager.getCurrentToken()
            .flatMap {
                Single.just(buildPeopleApi(it))
            }

    private fun buildPeopleApi(authToken: String): PeopleRemoteInterface =
        SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl, authToken).api

    private fun retryCriteria(attempts: Int, error: Throwable): Boolean =
        attempts < FirebaseManagerImpl.RETRY_ATTEMPTS_FOR_NETWORK_CALLS && errorIsWorthRetrying(error)

    private fun errorIsWorthRetrying(error: Throwable): Boolean =
        error is IOException ||
            error is HttpException && error.code() != 404 && error.code() !in 500..599

    private fun defaultResponseErrorHandling(e: HttpException): Nothing =
        when (e.code()) {
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
