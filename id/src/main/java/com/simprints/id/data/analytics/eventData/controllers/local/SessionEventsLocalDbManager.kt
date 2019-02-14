package com.simprints.id.data.analytics.eventData.controllers.local

import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsLocalDbManager {

    fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable
    fun loadSessions(projectId: String? = null, openSession: Boolean? = null): Single<ArrayList<SessionEvents>>
    /** @throws SessionNotFoundException */
    fun loadSessionById(sessionId: String): Single<SessionEvents>

    fun getSessionCount(projectId: String? = null): Single<Int>
    fun deleteSessions(projectId: String? = null,
                       sessionId: String? = null,
                       openSession: Boolean? = null,
                       startedBefore: Long? = null): Completable
}
