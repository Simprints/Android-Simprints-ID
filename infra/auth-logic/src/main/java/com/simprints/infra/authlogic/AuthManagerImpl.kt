package com.simprints.infra.authlogic

import com.simprints.infra.authlogic.authenticator.Authenticator
import com.simprints.infra.authlogic.authenticator.SignerManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import javax.inject.Inject

internal class AuthManagerImpl @Inject constructor(
    private val authenticator: Authenticator,
    private val securityStateScheduler: SecurityStateScheduler,
    private val signerManager: SignerManager,
) : AuthManager {

    override suspend fun authenticateSafely(userId: String, projectId: String, projectSecret: String, deviceId: String) =
        authenticator.authenticate(userId, projectId, projectSecret, deviceId)

    override fun startSecurityStateCheck() = securityStateScheduler.startSecurityStateCheck()

    override suspend fun signOut() = signerManager.signOut()
}
