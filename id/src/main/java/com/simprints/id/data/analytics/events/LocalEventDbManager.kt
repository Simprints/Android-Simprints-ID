package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import io.reactivex.Completable

/** @throws NotSignedInException */
interface LocalEventDbManager {

   fun initDb(localDbKey: LocalDbKey)
   fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable
}
