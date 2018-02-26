package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter
import io.reactivex.Single

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

    // DbManager call interception for populating arguments
    // Lifecycle
    fun initialiseDb(): Single<Unit>
    override fun signOut()

    // Data transfer
    fun savePerson(person: Person)
    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, callback: DataCallback?)
    fun getPeopleCount(group: Constants.GROUP): Long

    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
    fun updateIdentification(projectId: String, selectedGuid: String)

    fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalForm(refusalForm: RefusalForm)

    fun saveSession()

    fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>)
    fun syncUser(isInterrupted: () -> Boolean, emitter: Emitter<Progress>)

    fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback)
}
