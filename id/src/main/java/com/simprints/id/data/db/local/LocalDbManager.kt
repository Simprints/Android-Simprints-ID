package com.simprints.id.data.db.local

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.DownSyncParams
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Person
import com.simprints.id.domain.Project
import com.simprints.id.exceptions.safe.data.db.NoStoredLastSyncedInfoException
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Sort
import com.simprints.libcommon.Person as LibPerson

/** @throws NotSignedInException */
interface LocalDbManager {

    fun signInToLocal(localDbKey: LocalDbKey)

    // Data transfer
    // TODO: stop leaking Realm model into domain layer
    fun insertOrUpdatePersonInLocal(person: rl_Person): Completable

    fun insertOrUpdatePeopleInLocal(people: List<Person>): Completable

    fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                              gson: Gson,
                                              downSyncParams: DownSyncParams,
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
                            sortBy: Map<String, Sort>? = null): Single<List<Person>>

    fun loadPeopleFromLocalRx(patientId: String? = null,
                              userId: String? = null,
                              moduleId: String? = null,
                              toSync: Boolean? = null,
                              sortBy: Map<String, Sort>? = null): Flowable<Person>

    fun saveProjectIntoLocal(project: Project): Completable

    /** @throws NoSuchStoredProjectException */
    fun loadProjectFromLocal(projectId: String): Single<Project>

    //Sync
    fun updateSyncInfo(downSyncParams: DownSyncParams): Completable
    /** @throws NoStoredLastSyncedInfoException */
    fun getSyncInfoFor(typeSync: Constants.GROUP, specificModule: String?): Single<rl_SyncInfo>

    fun deletePeopleFromLocal(syncParams: SyncTaskParameters): Completable
    fun deleteSyncInfoFromLocal(syncParams: SyncTaskParameters): Completable
    fun loadPeopleFromLocal(destinationList: MutableList<LibPerson>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
}
