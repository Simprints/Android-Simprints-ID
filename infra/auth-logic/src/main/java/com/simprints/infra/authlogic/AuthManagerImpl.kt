package com.simprints.infra.authlogic

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authlogic.authenticator.Authenticator
import com.simprints.infra.authlogic.authenticator.SignerManager
import javax.inject.Inject

internal class AuthManagerImpl @Inject constructor(
    private val authenticator: Authenticator,
    private val signerManager: SignerManager,
) : AuthManager {
    override suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String,
    ) = authenticator.authenticate(
        userId = userId.asTokenizableRaw(),
        projectId = projectId,
        projectSecret = projectSecret,
        deviceId = deviceId,
    )

    override suspend fun signOut() = signerManager.signOut()
}
