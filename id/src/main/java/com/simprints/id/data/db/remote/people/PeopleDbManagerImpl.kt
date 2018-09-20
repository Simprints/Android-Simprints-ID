package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.exceptions.safe.data.db.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.extensions.handleResponse
import com.simprints.id.tools.extensions.handleResult
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException

open class PeopleDbManagerImpl(private val remoteDbManager: RemoteDbManager) : PeopleDbManager {

    override fun downloadPerson(patientId: String, projectId: String): Single<fb_Person> =
        remoteDbManager.getPeopleApiClient().flatMap {
            it.requestPerson(patientId, projectId)
                .retry(::retryCriteria)
                .handleResponse {
                    when (it.code()) {
                        404 -> throw DownloadingAPersonWhoDoesntExistOnServerException()
                        in 500..599 -> throw SimprintsInternalServerException()
                        else -> throw it
                    }
                }
        }

    override fun uploadPerson(fbPerson: fb_Person): Completable =
        uploadPeople(fbPerson.projectId, arrayListOf(fbPerson))

    override fun uploadPeople(projectId: String, patientsToUpload: ArrayList<fb_Person>): Completable =
        getPeopleApiClient().flatMapCompletable {
            it.uploadPeople(projectId, hashMapOf("patients" to patientsToUpload))
                .retry(::retryCriteria)
                .handleResult(::defaultResponseErrorHandling)
        }

    override fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> =
        getPeopleApiClient().flatMap {
            it.requestPeopleCount(syncParams.projectId, syncParams.userId, syncParams.moduleId)
                .retry(::retryCriteria)
                .handleResponse(::defaultResponseErrorHandling)
                .map { it.count }
        }

    override fun getPeopleApiClient(): Single<PeopleRemoteInterface> =
        remoteDbManager.getCurrentFirestoreToken()
            .flatMap {
                Single.just(buildPeopleApi(it))
            }

    private fun buildPeopleApi(authToken: String): PeopleRemoteInterface = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl, authToken).api

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
