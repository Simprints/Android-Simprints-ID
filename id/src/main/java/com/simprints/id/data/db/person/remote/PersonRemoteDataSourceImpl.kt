package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.PeopleOperationsParams
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes.FINGERPRINT
import com.simprints.id.data.db.person.remote.models.fromDomainToPostApi
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiLastKnownPatient
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperationGroup
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperationWhereLabel
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperations
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.WhereLabelKey.*
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.sumUp
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.EmptyPeopleOperationsParamsException
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

    override fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<PeopleOperationsParams>): Single<List<PeopleCount>> =
        if (peopleOperationsParams.isNotEmpty()) {
            makeRequestForPeopleOperations(projectId, peopleOperationsParams)
        } else {
            Single.error(EmptyPeopleOperationsParamsException())
        }

    private fun makeRequestForPeopleOperations(projectId: String, peopleOperationsParams: List<PeopleOperationsParams>): Single<List<PeopleCount>> =
        getPeopleApiClient().flatMap { peopleRemoteInterface ->

            peopleRemoteInterface.requestPeopleOperations(
                projectId,
                buildApiPeopleOperations(peopleOperationsParams))
                .retry(::retryCriteria)
                .handleResponse(::defaultResponseErrorHandling)
                .trace("countRequest")
                .map { response ->
                    response.groups.mapIndexed { index, responseGroup ->
                        val requestedSyncScope = peopleOperationsParams[index].subSyncScope
                        val countsForSyncScope = responseGroup.counts
                        PeopleCount(
                            projectId,
                            requestedSyncScope.userId,
                            requestedSyncScope.moduleId,
                            listOf(Modes.FINGERPRINT),
                            countsForSyncScope.sumUp())
                    }
                }
        }

    private fun buildApiPeopleOperations(peopleOperationsParams: List<PeopleOperationsParams>) =
        ApiPeopleOperations(buildGroups(peopleOperationsParams))

    private fun buildGroups(peopleOperationsParams: List<PeopleOperationsParams>) =
        peopleOperationsParams.map {
            val whereLabels = mutableListOf<ApiPeopleOperationWhereLabel>()
            val userId = it.subSyncScope.userId
            val moduleId = it.subSyncScope.moduleId

            if (userId?.isNotEmpty() == true) {
                whereLabels.add(ApiPeopleOperationWhereLabel(USER.key, userId))
            }

            if (moduleId?.isNotEmpty() == true) {
                whereLabels.add(ApiPeopleOperationWhereLabel(MODULE.key, moduleId))
            }

            whereLabels.add(ApiPeopleOperationWhereLabel(MODE.key, PipeSeparatorWrapperForURLListParam(FINGERPRINT).toString()))

            val lastKnownInfo = if (it.lastKnownPatientId?.isNotEmpty() == true && it.lastKnownPatientUpdatedAt != null) {
                ApiLastKnownPatient(it.lastKnownPatientId, it.lastKnownPatientUpdatedAt)
            } else {
                null
            }

            ApiPeopleOperationGroup(lastKnownInfo, whereLabels)
        }

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
