package com.simprints.id.data.db.project.remote

import com.google.gson.JsonElement
import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.tools.extensions.handleResponse
import com.simprints.id.tools.extensions.trace
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException


open class ProjectRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager): ProjectRemoteDataSource {

    override fun loadProjectFromRemote(projectId: String): Single<Project> =
        getProjectApiClient().flatMap {
            it.requestProject(projectId)
                .retry(::retryCriteria)
                .trace("requestProject")
                .handleResponse(::defaultResponseErrorHandling)
                .trace("requestProject")
        }

    override fun loadProjectRemoteConfigSettingsJsonString(projectId: String): Single<JsonElement> =
        getProjectApiClient().flatMap {
            it.requestProjectConfig(projectId)
                .retry(::retryCriteria)
                .trace("requestProjectConfig")
                .handleResponse(::defaultResponseErrorHandling)
                .trace("requestProjectConfig")
        }

    override fun getProjectApiClient(): Single<ProjectRemoteInterface> =
        remoteDbManager.getCurrentToken()
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
