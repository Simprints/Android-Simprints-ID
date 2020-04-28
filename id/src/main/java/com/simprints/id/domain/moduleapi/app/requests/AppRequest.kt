package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppIdentityConfirmationRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.moduleapi.app.requests.*
import kotlinx.android.parcel.Parcelize

fun IAppRequest.fromModuleApiToDomain(): AppRequest =
    when (this) {
        is IAppEnrollRequest ->
            AppEnrolRequest(projectId, userId, moduleId, metadata)

        is IAppIdentifyRequest ->
            AppIdentifyRequest(projectId, userId, moduleId, metadata)

        is IAppVerifyRequest ->
            AppVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid)

        is IAppIdentityConfirmationRequest ->
            AppIdentityConfirmationRequest(projectId, sessionId, selectedGuid, userId)

        else -> throw IllegalArgumentException("Request not recognised")
    }

sealed class AppRequest(open val projectId: String,
                        open val userId: String) : Parcelable {

    companion object {
        const val BUNDLE_KEY = "ApiRequest"
    }

    sealed class AppRequestFlow(override val projectId: String,
                                override val userId: String,
                                open val moduleId: String) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolRequest(override val projectId: String,
                                   override val userId: String,
                                   override val moduleId: String,
                                   val metadata: String) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        class AppIdentifyRequest(override val projectId: String,
                                 override val userId: String,
                                 override val moduleId: String,
                                 val metadata: String) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        data class AppVerifyRequest(override val projectId: String,
                                    override val userId: String,
                                    override val moduleId: String,
                                    val metadata: String,
                                    val verifyGuid: String) : AppRequestFlow(projectId, userId, moduleId)
    }

    @Parcelize
    data class AppIdentityConfirmationRequest(override val projectId: String,
                                              override val userId: String,
                                              val sessionId: String,
                                              val selectedGuid: String) : AppRequest(projectId, userId)
}
