package com.simprints.id.secure.models.remote

import androidx.annotation.Keep
import com.simprints.id.secure.models.Token
import java.io.Serializable

@Keep
data class ApiToken(
    val firebaseCustomToken: String = "",
    val firebaseOptions: FirebaseOptions
) : Serializable {

    @Keep
    data class FirebaseOptions(
        val projectId: String,
        val apiKey: String,
        val applicationId: String,
        val databaseUrl: String?,
        val gcmSenderId: String?,
        val storageBucket: String?
    )

    fun toDomainToken(): Token = Token(
        firebaseCustomToken,
        firebaseOptions.projectId,
        firebaseOptions.apiKey,
        firebaseOptions.applicationId
    )

}
