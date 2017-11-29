package com.simprints.id.data.prefs

import com.simprints.id.model.Callout
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

    // Session state
    var apiKey: String
    var callout: Callout
    var moduleId: String
    var userId: String
    var patientId: String
    var callingPackage: String
    var metadata: String
    var resultFormat: String

    // Settings
    var nudgeMode: Boolean
    var consent: Boolean
    var qualityThreshold: Int
    var returnIdCount: Int
    var language: String
    var languagePosition: Int
    var matcherType: Int
    var timeoutS: Int
    var appKey: String
    var syncGroup: Constants.GROUP
    var matchGroup: Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeS: Int
    var fingerStatusPersist: Boolean

    fun getFingerStatus(fingerIdentifier: FingerIdentifier): Boolean
    fun setFingerStatus(fingerIdentifier: FingerIdentifier, show: Boolean)

}