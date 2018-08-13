package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import io.reactivex.Completable
import io.reactivex.Single

/** @throws NotSignedInException */
interface SessionEventsLocalDbManager {

    fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable
    fun loadSessions(projectId: String? = null, openSession: Boolean? = null): Single<ArrayList<SessionEvents>>
    fun deleteSessions(projectId: String? = null): Completable
}
