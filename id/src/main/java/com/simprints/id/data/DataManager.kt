package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification


interface DataManager : PreferencesManager, DbManager, ApiManager,
        AnalyticsManager, SecureDataManager {

    val androidSdkVersion: Int
    val deviceModel: String
    val deviceId: String
    val appVersionName: String
    val libVersionName: String

    // Analytics
    fun logAlert(alertType: ALERT_TYPE)
    fun logUserProperties()
    fun logScannerProperties()
    fun logGuidSelectionService(apiKey: String, sessionId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean)
    fun logAuthStateChange(authenticated: Boolean)

    // DbManager argument interception for populating arguments
    // Remote Only
    fun updateIdentification(apiKey: String, selectedGuid: String)
    fun saveSession()
    // Local + remote which need to be split into smaller bits
    fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback)
    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>): Boolean
    fun saveRefusalForm(refusalForm: RefusalForm): Boolean
    fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    // Secure data
    fun getApiKeyOrEmpty(): String
}
