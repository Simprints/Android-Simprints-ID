package com.simprints.id.data

import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libcommon.Person
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification


interface DataManager : PreferencesManager, LocalDbManager, RemoteDbManager, ApiManager,
        AnalyticsManager, SecureDataManager {

    val androidSdkVersion: Int
    val deviceModel: String
    val deviceId: String
    val appVersionName: String
    val libVersionName: String

    // Analytics
    fun logAlert(alertType: ALERT_TYPE)
    fun logUserProperties()
    fun logLogin()
    fun logGuidSelectionService(selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean)
    fun logAuthStateChange(authenticated: Boolean)

    // Remote only
    fun isConnected(): Boolean
    fun registerAuthListener(authListener: AuthListener)
    fun registerConnectionListener(connectionListener: ConnectionListener)
    fun unregisterAuthListener(authListener: AuthListener)
    fun unregisterConnectionListener(connectionListener: ConnectionListener)
    fun updateIdentification(selectedGuid: String): Boolean

    // Local only
    fun getPeopleCount(group: Constants.GROUP): Long
    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP,
                   callback: DataCallback?)

    // Local + remote which need to be split into smaller bits
    fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback)
    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>): Boolean
    fun savePerson(person: Person): Boolean
    fun saveRefusalForm(refusalForm: RefusalForm): Boolean
    fun saveVerification(probe: Person, match: Verification?,
                         guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean
    fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback)

    // Local + remote + api which need to be split into smaller bits
    fun isInitialized(): Boolean
    fun initialize(callback: DataCallback)
    fun signIn(callback: DataCallback?)
    fun finish()
}
