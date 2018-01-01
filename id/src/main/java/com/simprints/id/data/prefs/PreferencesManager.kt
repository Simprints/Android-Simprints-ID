package com.simprints.id.data.prefs

import com.simprints.id.domain.Location
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier

/**
 * Why an interface if there is a single implementation?
 * Because using the interface makes it super easy to swap the true implementation (which uses the
 * Android Framework and is not a available in a non instrumented test) with a mock
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface PreferencesManager {

    // actionCallout parameters
    var calloutAction: CalloutAction
    var moduleId: String
    var userId: String
    var patientId: String
    var callingPackage: String
    var metadata: String
    var resultFormat: String
    var appKey: String

    // Other session state
    var sessionId: String
    var macAddress: String
    var hardwareVersion: Short
    var scannerId: String
    var location: Location
    var elapsedRealtimeOnSessionStart: Long
    var elapsedRealtimeOnLoadEnd: Long
    var elapsedRealtimeOnMainStart: Long
    var elapsedRealtimeOnMatchStart: Long
    var elapsedRealtimeOnSessionEnd: Long

    // Settings
    var nudgeMode: Boolean
    var consent: Boolean
    var qualityThreshold: Int
    var returnIdCount: Int
    var language: String
    var languagePosition: Int
    var matcherType: Int
    var timeoutS: Int
    var syncGroup: Constants.GROUP
    var matchGroup: Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    var fingerStatusPersist: Boolean
    var fingerStatus: Map<FingerIdentifier, Boolean>

    fun initializeSessionState(sessionId: String, elapsedRealtimeOnSessionStart: Long)

}
