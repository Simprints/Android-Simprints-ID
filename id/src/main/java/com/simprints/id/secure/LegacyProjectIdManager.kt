package com.simprints.id.secure

import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.models.LegacyProject
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class LegacyProjectIdManager(val client: SecureApiInterface) {

    fun requestLegacyProject(hashedLegacyProjectId: String): Single<LegacyProject> {
        return client.requestLegacyProject(hashedLegacyProjectId)
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
