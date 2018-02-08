package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter


interface DbManager : LocalDbManager, RemoteDbManager {

    // Local + remote which need to be split into smaller bits
    fun recoverLocalDb(deviceId: String, group: Constants.GROUP, callback: DataCallback)

    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>, sessionId: String): Boolean
    fun savePerson(person: Person): Boolean
    fun saveRefusalForm(refusalForm: RefusalForm, sessionId: String): Boolean
    fun saveVerification(probe: Person, patientId: String, match: Verification?, sessionId: String,
                         guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean

    fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback)

    // Local + remote + api which need to be split into smaller bits
    fun isInitialized(): Boolean
    fun initialize(callback: DataCallback)
    fun signIn()
    fun finish()

    fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>)
    fun syncUser(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>)

}
