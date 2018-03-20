package com.simprints.id.data.db.local

import com.simprints.id.libdata.DataCallback
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.libcommon.Person
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmConfiguration

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(projectId: String, localDbKey: LocalDbKey): Completable
    fun signOutOfLocal()
    fun isLocalDbInitialized(projectId: String): Boolean

    // Data transfer
    fun savePersonInLocal(fbPerson: fb_Person)
    fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback)
    fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
    fun getPeopleCountFromLocal(group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String): Long

    // Database instances
    fun getValidRealmConfig(): RealmConfiguration
    fun getRealmInstance(): Realm
}
