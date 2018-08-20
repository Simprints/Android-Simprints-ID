package com.simprints.id.data.analytics.eventData

import com.simprints.id.data.analytics.eventData.models.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventData.models.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Verification
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

    fun syncSessions(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Completable

    fun addGuidSelectionEventToLastIdentificationIfExists(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty(), selectedGuid: String): Completable
    fun addPersonCreationEventInBackground(person: Person)
    fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?)
    fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int)
    fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    fun addEventForCandidateReadInBackground(guid: String, startCandidateSearchTime: Long, localResult: CandidateReadEvent.LocalResult, remoteResult: CandidateReadEvent.RemoteResult)
}
