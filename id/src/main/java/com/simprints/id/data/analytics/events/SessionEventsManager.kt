package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsManager {

    val loginInfoManager: LoginInfoManager

    fun createSession(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Single<SessionEvents>
    fun getCurrentSession(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty() ): Single<SessionEvents>
    fun updateSession(block: (sessionEvents: SessionEvents) -> Unit,
                      projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Completable

    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit,
                                  projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty())

    fun insertOrUpdateSession(session: SessionEvents): Completable
}
