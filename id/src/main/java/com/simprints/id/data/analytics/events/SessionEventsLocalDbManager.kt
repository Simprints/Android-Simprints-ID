package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import io.reactivex.Completable
import io.reactivex.Single

/** @throws NotSignedInException */
interface SessionEventsLocalDbManager {

    fun initDb(localDbKey: LocalDbKey)
    fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable
    fun loadSessions(projectId: String): Single<ArrayList<SessionEvents>>
    fun loadLastOpenSession(projectId: String): Single<SessionEvents>
    fun deleteSessions(projectId: String): Completable
}
