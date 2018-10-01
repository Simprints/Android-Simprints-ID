package com.simprints.id.data.db.remote.project

import com.google.gson.JsonElement
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.ProjectRemoteInterface
import com.simprints.id.domain.Project
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.network.SimApiClient
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException


open class RemoteProjectManagerImpl(private val remoteDbManager: RemoteDbManager): RemoteProjectManager {

    override fun loadProjectFromRemote(projectId: String): Single<Project> =
        getProjectApiClient().flatMap {
            it.requestProject(projectId)
                .retry(::retryCriteria)
                .handleResponse(::defaultResponseErrorHandling)
        }

    override fun loadProjectRemoteConfigSettingsJsonString(projectId: String): Single<JsonElement> =
        getProjectApiClient().flatMap {
            it.requestProjectConfig(projectId)
                .retry(::retryCriteria)
                .handleResponse(::defaultResponseErrorHandling)
        }

    override fun getProjectApiClient(): Single<ProjectRemoteInterface> =
        remoteDbManager.getCurrentFirestoreToken()
            .flatMap {
                Single.just(buildProjectApi(it))
            }

    private fun buildProjectApi(authToken: String): ProjectRemoteInterface =
        SimApiClient(ProjectRemoteInterface::class.java, ProjectRemoteInterface.baseUrl, authToken).api

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
