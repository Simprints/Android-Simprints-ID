package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.toApiPostPerson
import com.simprints.id.data.db.person.remote.models.toDomainPeopleCount
import com.simprints.id.data.db.person.remote.models.toDomainPerson
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.unexpected.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
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
                .map(ApiGetPerson::toDomainPerson)
        }

    override fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable =
        getPeopleApiClient().flatMapCompletable {
            it.uploadPeople(projectId, hashMapOf("patients" to patientsToUpload.map(Person::toApiPostPerson)))
                .retry(::retryCriteria)
                .trace("uploadPatientBatch")
                .handleResult(::defaultResponseErrorHandling)
                .trace("uploadPatientBatch")
        }

    override fun getDownSyncPeopleCount(syncScope: SyncScope): Single<List<PeopleCount>> =
        getPeopleApiClient().flatMap { peopleRemoteInterface ->
            peopleRemoteInterface.requestPeopleCount(syncScope.projectId, syncScope.userId,
                syncScope.moduleIds?.toTypedArray()?.let { PipeSeparatorWrapperForURLListParam(*it) })
                .retry(::retryCriteria)
                .handleResponse(::defaultResponseErrorHandling)
                .trace("countRequest")
                .map { apiPeopleCount -> apiPeopleCount.map { it.toDomainPeopleCount() } }
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
