package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Constants
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification

interface DataManager : PreferencesManager, DbManager,
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

    // DbManager call interception for populating arguments
    // Lifecycle
    override fun initialiseDb()
    override fun signOut()

    // Data transfer
    fun savePerson(person: Person)
    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, callback: DataCallback?)
    fun getPeopleCount(group: Constants.GROUP): Int

    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
    fun updateIdentification(projectId: String, selectedGuid: String)

    fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalForm(refusalForm: RefusalForm)

    fun saveSession()

    fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback)
}
