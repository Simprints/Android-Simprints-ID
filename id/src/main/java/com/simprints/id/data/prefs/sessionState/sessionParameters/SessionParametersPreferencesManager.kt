package com.simprints.id.data.prefs.sessionState.sessionParameters

import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.sessionParameters.SessionParameters

interface SessionParametersPreferencesManager {

    var calloutAction: CalloutAction
    var projectId: String
    var moduleId: String
    var userId: String
    @Deprecated("we are dropping UPDATE, so it should be set only during enrol")
    var patientId: String
    var callingPackage: String
    var metadata: String
    var resultFormat: String

    var sessionParameters: SessionParameters
        set(value) {
            calloutAction = value.calloutAction
            projectId = value.projectId
            moduleId = value.moduleId
            userId = value.userId
            patientId = value.patientId
            callingPackage = value.callingPackage
            metadata = value.metadata
            resultFormat = value.resultFormat
        }
        get() {
            throw NotImplementedError()
        }

    fun resetSessionParameters()
}
