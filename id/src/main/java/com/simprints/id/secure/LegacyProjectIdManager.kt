package com.simprints.id.secure

import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.models.ProjectId
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class LegacyProjectIdManager(val client: ApiServiceInterface) {

    companion object {
        private const val hashedLegacyProjectIdKey = "X-LegacyIdMD5"
    }

    fun requestProjectId(hashedLegacyProjectId: String): Single<ProjectId> {
        val headers = mapOf(
            LegacyProjectIdManager.hashedLegacyProjectIdKey to hashedLegacyProjectId)

        return client.projectId(headers)
            .handleResponse(::handleResponseError)
            .subscribeOn(Schedulers.io())
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            404 -> throw InvalidLegacyProjectIdReceivedFromIntentException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
