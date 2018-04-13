package com.simprints.id.data.db.local

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.models.Project
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Person
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Sort

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(): Completable

    // Data transfer
    fun insertOrUpdatePersonInLocal(person: rl_Person): Completable

    fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                              gson: Gson,
                                              syncParams: SyncTaskParameters,
                                              shouldStop: (personSaved: fb_Person) -> Boolean): Completable

    fun getPeopleCountFromLocal(patientId: String? = null,
                                userId: String? = null,
                                moduleId: String? = null,
                                toSync: Boolean? = null): Single<Int>

    fun loadPersonFromLocal(personId: String): Single<Person>

    fun loadPeopleFromLocal(patientId: String? = null,
                            userId: String? = null,
                            moduleId: String? = null,
                            toSync: Boolean? = null,
                            sortBy: Map<String, Sort>? = null): Single<ArrayList<rl_Person>>

    fun loadPeopleFromLocalRx(patientId: String? = null,
                              userId: String? = null,
                              moduleId: String? = null,
                              toSync: Boolean? = null,
                              sortBy: Map<String, Sort>? = null): Flowable<rl_Person>

    fun saveProjectIntoLocal(project: Project): Completable
    fun loadProjectFromLocal(projectId: String): Single<Project>

    //Sync
    fun getSyncInfoFor(typeSync: Constants.GROUP): Single<rl_SyncInfo>

    fun deletePeopleFromLocal(syncParams: SyncTaskParameters)
    fun deleteSyncInfoFromLocal(syncParams: SyncTaskParameters)
    fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
}
