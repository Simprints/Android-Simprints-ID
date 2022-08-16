package com.simprints.infra.login.remote.models

import androidx.annotation.Keep
import com.simprints.infra.login.domain.models.Token

@Keep
internal  data class ApiToken(val firebaseCustomToken: String = "", val firebaseOptions: FirebaseOptions) {

    @Keep
    data class FirebaseOptions(
        val projectId: String,
        val apiKey: String,
        val applicationId: String,
        val databaseUrl: String?,
        val gcmSenderId: String?,
        val storageBucket: String?
    )

    fun toDomain(): Token = Token(
        firebaseCustomToken,
        firebaseOptions.projectId,
        firebaseOptions.apiKey,
        firebaseOptions.applicationId
    )
}
