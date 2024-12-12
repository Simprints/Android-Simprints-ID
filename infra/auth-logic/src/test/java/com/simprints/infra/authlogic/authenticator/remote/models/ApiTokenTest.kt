package com.simprints.infra.authlogic.authenticator.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.domain.models.Token
import org.junit.Test

class ApiTokenTest {
    @Test
    fun `should map the model correctly`() {
        val apiToken = ApiToken(
            firebaseCustomToken = "token",
            firebaseOptions = ApiToken.FirebaseOptions(
                projectId = "projectId",
                apiKey = "apiKey",
                applicationId = "applicationId",
                databaseUrl = "databaseUrl",
                gcmSenderId = "gcmSenderId",
                storageBucket = "storageBucket",
            ),
        )

        val token = Token(
            value = "token",
            projectId = "projectId",
            apiKey = "apiKey",
            applicationId = "applicationId",
        )
        assertThat(apiToken.toDomain()).isEqualTo(token)
    }
}
