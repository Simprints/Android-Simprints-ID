package com.simprints.id.data.analytics.eventData

import com.simprints.id.data.analytics.eventData.models.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventData.models.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsManager {

    val loginInfoManager: LoginInfoManager

    fun createSession(): Single<SessionEvents>
    fun getCurrentSession(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty() ): Single<SessionEvents>

    fun updateSession(block: (sessionEvents: SessionEvents) -> Unit,
                      projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Completable

    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit,
                                  projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty())

    fun getSessionCount(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Single<Int>

    /**
     * @throws NoSessionsFoundException
     * @throws SessionUploadFailureException
     */
    fun syncSessions(projectId: String = loginInfoManager.getSignedInProjectIdOrEmpty()): Completable

    fun addGuidSelectionEventToLastIdentificationIfExists(selectedGuid: String, sessionId: String): Completable
    fun addPersonCreationEventInBackground(person: Person)
    fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?)
    fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int)
    fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
    fun addLocationToSession(latitude: Double, longitude: Double)

    fun addEventForCandidateReadInBackground(guid: String, startCandidateSearchTime: Long, localResult: CandidateReadEvent.LocalResult, remoteResult: CandidateReadEvent.RemoteResult)
}
