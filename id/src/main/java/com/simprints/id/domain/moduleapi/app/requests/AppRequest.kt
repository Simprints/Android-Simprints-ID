package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcel
import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step
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
        data class AppIdentifyRequest(override val projectId: String,
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

    sealed class AppRequestFollowUp(override val projectId: String,
                                    override val userId: String) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolLastBiometricsRequest(override val projectId: String,
                                                 override val userId: String,
                                                 val moduleId: String,
                                                 val metadata: String,
                                                 val identificationSessionId: String) : AppRequest(projectId, userId)


        @Parcelize
        data class AppConfirmIdentityRequest(override val projectId: String,
                                             override val userId: String,
                                             val sessionId: String,
                                             val selectedGuid: String) : AppRequest(projectId, userId)
    }
}
