package com.simprints.id.data.db.local.realm

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.LocalDbKeyProvider
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.models.Project
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Person
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort

class RealmDbManagerImpl(private val appContext: Context,
                         private val localDbKeyProvider: LocalDbKeyProvider) : LocalDbManager {

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"

        const val USER_ID_FIELD = "userId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
        const val UPDATE_TIME_FIELD = "updatedAt"
    }

    private var realmConfig: RealmConfiguration? = null

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(): Completable = getLocalDbKey().flatMapCompletable {
        EncryptionMigration(it, appContext)
        getRealmInstance().map { realm -> realm.use { } }.toCompletable()
    }

    override fun insertOrUpdatePersonInLocal(person: rl_Person): Completable =
        getRealmInstance().flatMapCompletable {
            it.use {
                it.executeTransaction { it.insertOrUpdate(person) }.let { Completable.complete() }
            }
        }

    override fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                                       gson: Gson,
                                                       syncParams: SyncTaskParameters,
                                                       shouldStop: (personSaved: fb_Person) -> Boolean): Completable =
        getRealmInstance().map {
            it.use {
                //TODO Try to throw and exception in the method to see if it's caught in the caller
                it.executeTransaction {
                    while (readerOfPeopleArray.hasNext()) {

                        val lastPersonSaved = parseFromStreamAndSavePerson(gson, readerOfPeopleArray, it)
                        it.insertOrUpdate(rl_SyncInfo(syncParams.toGroup(), rl_Person(lastPersonSaved)))

                        if (shouldStop(lastPersonSaved)) {
                            break
                        }
                    }
                }
            }
            updateSyncInfo(syncParams)
        }.toCompletable()

    override fun getPeopleCountFromLocal(patientId: String?,
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Single<Int> = getRealmInstance().map {
        it.use { buildQueryForPerson(it, patientId, userId, moduleId, toSync).count().toInt() }
    }

    override fun loadPersonFromLocal(personId: String): Single<Person> = getRealmInstance().map {
        it.use {
            it.where(rl_Person::class.java).equalTo(PATIENT_ID_FIELD, personId).findFirst().let {
                if (it != null)
                    return@map it.libPerson
                else throw IllegalStateException()
            }
        }
    }

    override fun loadPeopleFromLocal(patientId: String?,
                                     userId: String?,
                                     moduleId: String?,
                                     toSync: Boolean?,
                                     sortBy: Map<String, Sort>?): Single<ArrayList<rl_Person>> =
        getRealmInstance().map {
            val query = buildQueryForPerson(it, patientId, userId, moduleId, toSync, sortBy)
            ArrayList(it.copyFromRealm(query.findAll(), 4))
        }

    override fun loadPeopleFromLocalRx(patientId: String?,
                                       userId: String?,
                                       moduleId: String?,
                                       toSync: Boolean?,
                                       sortBy: Map<String, Sort>?): Flowable<rl_Person> =

        getRealmInstance().flatMapPublisher {
            Flowable.create<rl_Person>({ emitter ->
                it.use {
                    val query = buildQueryForPerson(it, patientId, userId, moduleId, toSync, sortBy)
                    val people = query.findAll()
                    for (person in people) {
                        emitter.onNext(person)
                    }
                    emitter.onComplete()
                }
            }, BackpressureStrategy.BUFFER)
        }

    override fun getSyncInfoFor(typeSync: Constants.GROUP): Single<rl_SyncInfo> =
        getRealmInstance().map {
            it.use { realm ->
                realm.where(rl_SyncInfo::class.java).equalTo(SYNC_ID_FIELD, typeSync.ordinal).findFirst()?.let {
                    realm.copyFromRealm(it)
                } ?: throw Exception("Not found") //StopShip: Exception
            }
        }

    override fun loadProjectFromLocal(projectId: String): Single<Project> =
        getRealmInstance().map {
            it.use { realm ->
                realm.where(Project::class.java).equalTo(Project.PROJECT_ID_FIELD, projectId).findFirst()?.let {
                    realm.copyFromRealm(it)
                } ?: throw Exception("Not found")
            }
        }

    override fun deletePeopleFromLocal(syncParams: SyncTaskParameters): Completable =
        getRealmInstance().flatMapCompletable {
            it.use {
                it.executeTransaction {
                    val query = buildQueryForPerson(it, syncParams).findAll()
                    query.deleteAllFromRealm()
                }
            }
            Completable.complete()
        }

    override fun deleteSyncInfoFromLocal(syncParams: SyncTaskParameters): Completable =
        getRealmInstance().flatMapCompletable {
            it.use {
                it.executeTransaction {
                    val query = it.where(rl_SyncInfo::class.java)
                        .equalTo(rl_SyncInfo.SYNC_ID_FIELD, syncParams.toGroup().ordinal)
                        .findAll()
                    query.deleteAllFromRealm()
                }
            }
            Completable.complete()
        }

    override fun saveProjectIntoLocal(project: Project): Completable =
        getRealmInstance().flatMapCompletable {
            it.use {
                it.executeTransaction {
                    it.insertOrUpdate(project)
                }
                Completable.complete()
            }
        }

    private fun getLocalDbKey(): Single<LocalDbKey> = localDbKeyProvider.getLocalDbKey()

    private fun getRealmConfig(): Single<RealmConfiguration> = realmConfig.let {
        return if (it == null) {
            getLocalDbKey().flatMap { createAndSaveRealmConfig(it) }
        } else {
            Single.just(it)
        }
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): Single<RealmConfiguration> =
        Single.just(RealmConfig.get(localDbKey.projectId, localDbKey.value)
            .also { realmConfig = it })

    private fun getRealmInstance(): Single<Realm> = getRealmConfig()
        .flatMap {
            Single.just(Realm.getInstance(it))
        }

    private fun buildQueryForPerson(realm: Realm,
                                    patientId: String? = null,
                                    userId: String? = null,
                                    moduleId: String? = null,
                                    toSync: Boolean? = null,
                                    sortBy: Map<String, Sort>? = null): RealmQuery<rl_Person> {

        return realm.where(rl_Person::class.java).apply {
            patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
            userId?.let { this.equalTo(USER_ID_FIELD, it) }
            moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
            toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
            sortBy?.let { this.sort(sortBy.keys.toTypedArray(), sortBy.values.toTypedArray()) }
        }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    syncParams: SyncTaskParameters): RealmQuery<rl_Person> = buildQueryForPerson(
        realm = realm,
        userId = syncParams.userId,
        moduleId = syncParams.moduleId
    )

    private fun updateSyncInfo(syncParams: SyncTaskParameters): Completable =
        getRealmInstance().map { realm ->
            buildQueryForPerson(realm, syncParams)
                .sort(UPDATE_TIME_FIELD, Sort.DESCENDING)
                .findAll()
                .first()?.let { person ->
                    realm.executeTransaction {
                        it.insertOrUpdate(rl_SyncInfo(syncParams.toGroup(), person))
                    }
                }
        }.toCompletable()

    private fun parseFromStreamAndSavePerson(gson: Gson,
                                             readerOfPersonsArray: JsonReader,
                                             realm: Realm): fb_Person {
        return gson.fromJson<fb_Person>(readerOfPersonsArray, fb_Person::class.java).apply {
            realm.insertOrUpdate(rl_Person(this))
        }
    }

    override fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        val result = when (group) {
            Constants.GROUP.GLOBAL -> loadPeopleFromLocal().blockingGet().map { it.libPerson }
            Constants.GROUP.USER -> loadPeopleFromLocal(userId = userId).blockingGet().map { it.libPerson }
            Constants.GROUP.MODULE -> loadPeopleFromLocal(moduleId = moduleId).blockingGet().map { it.libPerson }
        }
        destinationList.addAll(result)
        callback?.onSuccess()
    }
}
