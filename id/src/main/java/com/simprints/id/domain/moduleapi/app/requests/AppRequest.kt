package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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
    data class AppConfirmIdentityRequest(override val projectId: String,
                                         override val userId: String,
                                         val sessionId: String,
                                         val selectedGuid: String) : AppRequest(projectId, userId)
}
