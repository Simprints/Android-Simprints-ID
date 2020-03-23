package com.simprints.id.activities.login.repository

import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.TimeHelper

class LoginRepositoryImpl(
    private val projectAuthenticator: ProjectAuthenticator,
    private val authenticationHelper: AuthenticationHelper,
    private val sessionRepo: SessionRepository,
    private val timeHelper: TimeHelper
) : LoginRepository {

    private var loginStartTime = 0L

    override suspend fun authenticate(
        projectId: String,
        userId: String,
        projectSecret: String
    ): AuthenticationEvent.Result {
        return authenticationHelper.authenticateSafely(authBlock = {
            loginStartTime = timeHelper.now()
            val nonceScope = NonceScope(projectId, userId)
            projectAuthenticator.authenticate(nonceScope, projectSecret)
        }, after = {
            addEventAndUpdateProjectIdIfRequired(it, projectId, userId)
        })
    }

    private fun addEventAndUpdateProjectIdIfRequired(
        result: AuthenticationEvent.Result,
        suppliedProjectId: String,
        suppliedUserId: String
    ) {
        val event = AuthenticationEvent(
            loginStartTime,
            timeHelper.now(),
            AuthenticationEvent.UserInfo(suppliedProjectId, suppliedUserId),
            result
        )
        sessionRepo.addEventToCurrentSessionInBackground(event)
    }

}
