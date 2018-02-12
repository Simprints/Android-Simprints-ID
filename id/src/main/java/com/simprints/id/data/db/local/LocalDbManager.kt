package com.simprints.id.data.db.local

import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.tools.Constants

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(localDbKey: String)
    fun signOutOfLocal()
    fun isLocalDbInitialized(): Boolean

    // Data transfer
    fun savePersonInLocal(fbPerson: fb_Person)
    fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback)
    fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
    fun getPeopleCountFromLocal(group: Constants.GROUP, userId: String, moduleId: String): Long
}
