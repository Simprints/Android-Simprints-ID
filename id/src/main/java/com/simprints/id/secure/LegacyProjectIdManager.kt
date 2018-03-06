package com.simprints.id.secure

import com.simprints.id.exceptions.safe.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.secure.models.ProjectId
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response


class LegacyProjectIdManager(val client: ApiServiceInterface) {

    companion object {
        private const val hashedLegacyProjectIdKey = "X-LegacyIdMD5"
    }

    fun requestProjectId(hashedLegacyProjectId: String): Single<ProjectId> {
        val headers = mapOf(
            LegacyProjectIdManager.hashedLegacyProjectIdKey to hashedLegacyProjectId)

        return client.projectId(headers).handleResponse().subscribeOn(Schedulers.io())
    }

    private fun Single<out Response<ProjectId>>.handleResponse(): Single<ProjectId> =
        flatMap { response ->
            if (!response.isSuccessful) handleResponseError(HttpException(response))
            Single.just(response.body())
        }

    private fun handleResponseError(e: HttpException) {
        if (e.code() == 404) throw InvalidLegacyProjectIdReceivedFromIntentException()
        else throw e
    }
}
