package com.simprints.id.data.db.local

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmConfiguration

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(projectId: String, localDbKey: LocalDbKey): Completable
    fun signOutOfLocal()
    fun isLocalDbInitialized(projectId: String): Boolean

    // Data transfer
    fun insertOrUpdatePersonInLocal(person: rl_Person): Completable
    fun savePeopleFromStream(reader: JsonReader, gson: Gson, groupSync: Constants.GROUP, shouldStop: () -> Boolean)

    fun getPersonsCountFromLocal(patientId: String? = null,
                                 projectId: String? = null,
                                 userId: String? = null,
                                 moduleId: String? = null,
                                 toSync: Boolean? = null): Int

    fun loadPersonsFromLocal(patientId: String? = null,
                             projectId: String? = null,
                             userId: String? = null,
                             moduleId: String? = null,
                             toSync: Boolean? = null): ArrayList<rl_Person>

    //Sync
    fun getSyncInfoFor(typeSync: Constants.GROUP): RealmSyncInfo?

    // Database instances
    fun getValidRealmConfig(): RealmConfiguration

    fun getRealmInstance(): Realm
    fun updateSyncInfo(syncParams: SyncTaskParameters)
}
