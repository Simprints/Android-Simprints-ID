package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.core.domain.tokenization.TokenizedString
import kotlinx.parcelize.Parcelize

sealed class AppRequest(
    open val projectId: String,
    open val userId: TokenizedString
) : Parcelable {

    companion object {
        const val BUNDLE_KEY = "ApiRequest"
    }

    sealed class AppRequestFlow(
        override val projectId: String,
        override val userId: TokenizedString,
        open val moduleId: String
    ) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolRequest(
            override val projectId: String,
            override val userId: TokenizedString,
            override val moduleId: String,
            val metadata: String
        ) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        data class AppIdentifyRequest(
            override val projectId: String,
            override val userId: TokenizedString,
            override val moduleId: String,
            val metadata: String
        ) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        data class AppVerifyRequest(
            override val projectId: String,
            override val userId: TokenizedString,
            override val moduleId: String,
            val metadata: String,
            val verifyGuid: String
        ) : AppRequestFlow(projectId, userId, moduleId)
    }

    sealed class AppRequestFollowUp(
        override val projectId: String,
        override val userId: TokenizedString
    ) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolLastBiometricsRequest(
            override val projectId: String,
            override val userId: TokenizedString,
            val moduleId: String,
            val metadata: String,
            val identificationSessionId: String
        ) : AppRequestFollowUp(projectId, userId)


        @Parcelize
        data class AppConfirmIdentityRequest(
            override val projectId: String,
            override val userId: TokenizedString,
            val sessionId: String,
            val selectedGuid: String
        ) : AppRequestFollowUp(projectId, userId)
    }
}
