package com.simprints.id.data.analytics.eventData

import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.exceptions.safe.session.SessionNotFoundException
import io.reactivex.Completable
import io.reactivex.Single


interface SessionEventsLocalDbManager {

    fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable
    fun loadSessions(projectId: String? = null, openSession: Boolean? = null): Single<ArrayList<SessionEvents>>
    /** @throws SessionNotFoundException */
    fun loadSessionById(sessionId: String): Single<SessionEvents>
    fun deleteSessions(projectId: String? = null, openSession: Boolean? = null): Completable
}
