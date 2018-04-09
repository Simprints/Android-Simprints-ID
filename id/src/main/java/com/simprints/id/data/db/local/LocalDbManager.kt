package com.simprints.id.data.db.local

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(localDbKey: LocalDbKey): Completable

    fun signOutOfLocal()

    fun isLocalDbInitialized(projectId: String): Boolean

    // Data transfer
    fun insertOrUpdatePersonInLocal(person: rl_Person): Completable

    fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                              gson: Gson,
                                              syncParams: SyncTaskParameters,
                                              shouldStop: (personSaved: fb_Person) -> Boolean)

    fun getPeopleCountFromLocal(patientId: String? = null,
                                projectId: String? = null,
                                userId: String? = null,
                                moduleId: String? = null,
                                toSync: Boolean? = null): Int

    fun loadPeopleFromLocal(patientId: String? = null,
                            projectId: String? = null,
                            userId: String? = null,
                            moduleId: String? = null,
                            toSync: Boolean? = null): ArrayList<rl_Person>

    fun loadPeopleFromLocalRx(patientId: String? = null,
                              projectId: String? = null,
                              userId: String? = null,
                              moduleId: String? = null,
                              toSync: Boolean? = null): Flowable<rl_Person>

    //Sync
    fun getSyncInfoFor(typeSync: Constants.GROUP): RealmSyncInfo?

    // Database instances
    fun getValidRealmConfig(): RealmConfiguration

    fun getRealmInstance(): Realm
}
