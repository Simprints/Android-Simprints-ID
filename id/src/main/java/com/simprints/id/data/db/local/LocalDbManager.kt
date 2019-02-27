package com.simprints.id.data.db.local

import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.DbSyncInfo
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.Project
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Sort

/** @throws NotSignedInException */
interface LocalDbManager {

    fun signInToLocal(localDbKey: LocalDbKey)

    // Data transfer
    // TODO: stop leaking Realm model into domain layer
    fun insertOrUpdatePersonInLocal(person: Person): Completable

    fun insertOrUpdatePeopleInLocal(people: List<Person>): Completable

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

    fun deletePeopleFromLocal(syncScope: SyncScope): Completable
    fun deletePeopleFromLocal(subSyncScope: SubSyncScope): Completable
    fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: GROUP, userId: String, moduleId: String, callback: DataCallback?)

    //@Deprecated: do not use it. Use Room SyncStatus
    fun getRlSyncInfo(subSyncScope: SubSyncScope): Single<DbSyncInfo>
    fun deleteSyncInfo(subSyncScope: SubSyncScope): Completable
}
