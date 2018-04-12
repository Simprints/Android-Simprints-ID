package com.simprints.id.data.db.local

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Person
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(): Completable

    // Data transfer
    fun insertOrUpdatePersonInLocal(person: rl_Person): Completable

    fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                              gson: Gson,
                                              syncParams: SyncTaskParameters,
                                              shouldStop: (personSaved: fb_Person) -> Boolean)

    fun getPeopleCountFromLocal(patientId: String? = null,
                                userId: String? = null,
                                moduleId: String? = null,
                                toSync: Boolean? = null): Single<Int>

    fun loadPersonFromLocal(personId: String): Single<Person>

    fun loadPeopleFromLocal(patientId: String? = null,
                            userId: String? = null,
                            moduleId: String? = null,
                            toSync: Boolean? = null): Single<ArrayList<rl_Person>>

    fun loadPeopleFromLocalRx(patientId: String? = null,
                              userId: String? = null,
                              moduleId: String? = null,
                              toSync: Boolean? = null): Flowable<rl_Person>

    //Sync
    fun getSyncInfoFor(typeSync: Constants.GROUP): Single<rl_SyncInfo>

}
