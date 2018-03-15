package com.simprints.id.data.prefs.sessionState.sessionParameters

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.delegates.ComplexPreference
import com.simprints.id.tools.delegates.PrimitivePreference
import com.simprints.id.tools.serializers.Serializer

class SessionParametersPreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                                              calloutActionSerializer: Serializer<CalloutAction>)
    : SessionParametersPreferencesManager {

    companion object {

        private val CALLOUT_ACTION_KEY = "CalloutAction"
        private val CALLOUT_ACTION_DEFAULT = CalloutAction.MISSING

        private val PROJECT_ID_KEY = "ProjectId"
        private val PROJECT_ID_DEFAULT = ""

        private val MODULE_ID_KEY = "ModuleId"
        private val MODULE_ID_DEFAULT = ""

        private val USER_ID_KEY = "UserId"
        private val USER_ID_DEFAULT = ""

        private val PATIENT_ID_KEY = "PatientId"
        private val PATIENT_ID_DEFAULT = ""

        private val CALLING_PACKAGE_KEY = "CallingPackage"
        private val CALLING_PACKAGE_DEFAULT = ""

        private val METADATA_KEY = "Metadata"
        private val METADATA_DEFAULT = ""

        private val RESULT_FORMAT_KEY = "ResultFormat"
        private val RESULT_FORMAT_DEFAULT = ""
    }

    // CalloutAction of the current session
    override var calloutAction: CalloutAction
        by ComplexPreference(prefs, CALLOUT_ACTION_KEY, CALLOUT_ACTION_DEFAULT, calloutActionSerializer)

    // Project Id
    override var projectId: String
        by PrimitivePreference(prefs, PROJECT_ID_KEY, PROJECT_ID_DEFAULT)

    // Module ID of the current session
    override var moduleId: String
        by PrimitivePreference(prefs, MODULE_ID_KEY, MODULE_ID_DEFAULT)

    // User ID of the current session
    override var userId: String
        by PrimitivePreference(prefs, USER_ID_KEY, USER_ID_DEFAULT)

    // Patient ID of the current session
    override var patientId: String
        by PrimitivePreference(prefs, PATIENT_ID_KEY, PATIENT_ID_DEFAULT)

    // Calling package of the current session
    override var callingPackage: String
        by PrimitivePreference(prefs, CALLING_PACKAGE_KEY, CALLING_PACKAGE_DEFAULT)

    // Metadata of the current session
    override var metadata: String
        by PrimitivePreference(prefs, METADATA_KEY, METADATA_DEFAULT)

    // Result format of the current session
    override var resultFormat: String
        by PrimitivePreference(prefs, RESULT_FORMAT_KEY, RESULT_FORMAT_DEFAULT)

    override fun resetSessionParameters() {
        calloutAction = CALLOUT_ACTION_DEFAULT
        moduleId = MODULE_ID_DEFAULT
        userId = USER_ID_DEFAULT
        patientId = PATIENT_ID_DEFAULT
        callingPackage = CALLING_PACKAGE_DEFAULT
        metadata = METADATA_DEFAULT
        resultFormat = RESULT_FORMAT_DEFAULT
    }
}
