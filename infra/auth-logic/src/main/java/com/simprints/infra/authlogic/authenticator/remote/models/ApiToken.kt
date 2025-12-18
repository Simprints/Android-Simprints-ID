package com.simprints.infra.authlogic.authenticator.remote.models

import androidx.annotation.Keep
import com.simprints.infra.authstore.domain.models.Token
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiToken(
    val firebaseCustomToken: String = "",
    val firebaseOptions: FirebaseOptions,
) {
    @Keep
    @Serializable
    data class FirebaseOptions(
        val projectId: String,
        val apiKey: String,
        val applicationId: String,
        val databaseUrl: String? = null,
        val gcmSenderId: String? = null,
        val storageBucket: String? = null,
    )

    fun toDomain(): Token = Token(
        firebaseCustomToken,
        firebaseOptions.projectId,
        firebaseOptions.apiKey,
        firebaseOptions.applicationId,
    )
}
