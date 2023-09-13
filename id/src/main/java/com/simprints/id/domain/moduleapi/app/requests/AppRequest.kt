package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize

sealed class AppRequest(
    open val projectId: String,
    open val userId: TokenizableString
) : Parcelable {

    companion object {
        const val BUNDLE_KEY = "ApiRequest"
    }

    sealed class AppRequestFlow(
        override val projectId: String,
        override val userId: TokenizableString,
        open val moduleId: TokenizableString
    ) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolRequest(
            override val projectId: String,
            override val userId: TokenizableString,
            override val moduleId: TokenizableString,
            val metadata: String
        ) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        data class AppIdentifyRequest(
            override val projectId: String,
            override val userId: TokenizableString,
            override val moduleId: TokenizableString,
            val metadata: String
        ) : AppRequestFlow(projectId, userId, moduleId)

        @Parcelize
        data class AppVerifyRequest(
            override val projectId: String,
            override val userId: TokenizableString,
            override val moduleId: TokenizableString,
            val metadata: String,
            val verifyGuid: String
        ) : AppRequestFlow(projectId, userId, moduleId)
    }

    sealed class AppRequestFollowUp(
        override val projectId: String,
        override val userId: TokenizableString
    ) : AppRequest(projectId, userId) {

        @Parcelize
        data class AppEnrolLastBiometricsRequest(
            override val projectId: String,
            override val userId: TokenizableString,
            val moduleId: TokenizableString,
            val metadata: String,
            val identificationSessionId: String
        ) : AppRequestFollowUp(projectId, userId)


        @Parcelize
        data class AppConfirmIdentityRequest(
            override val projectId: String,
            override val userId: TokenizableString,
            val sessionId: String,
            val selectedGuid: String
        ) : AppRequestFollowUp(projectId, userId)
    }
}
