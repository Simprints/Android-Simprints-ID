package com.simprints.id.data.db.local

import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.DatabaseContext
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification

interface LocalDbManager {

    fun getPeopleCountFromLocal(group: Constants.GROUP): Long

    fun loadPeopleFromLocal(destinationList: MutableList<Person>,
                   group: Constants.GROUP, callback: DataCallback?)

    fun recoverLocalDbGetFile(deviceId: String, group: Constants.GROUP, callback: DataCallback)

    fun saveIdentificationInLocal(probe: Person, matchSize: Int, matches: List<Identification>, sessionId: String): Boolean
    fun savePersonInLocal(person: Person): Boolean
    fun saveRefusalFormInLocal(refusalForm: RefusalForm, sessionId: String): Boolean
    fun saveVerificationInLocal(probe: Person, patientId: String, match: Verification?, sessionId: String,
                         guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean
    fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback)

    fun isLocalDbInitialized(): Boolean
    fun initializeLocalDb(callback: DataCallback)
    fun signInToLocal(localDbKey: String)
    fun finishLocalDb()
}
