package com.simprints.id.data.db.local.realm

import android.content.Context
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.*
import com.simprints.id.data.db.local.realm.models.adapters.toProject
import com.simprints.id.data.db.local.realm.models.adapters.toRealmProject
import com.simprints.id.domain.*
import com.simprints.id.exceptions.safe.data.db.NoSuchRlSessionInfoException
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort
import timber.log.Timber
import com.simprints.id.domain.fingerprint.Person as LibPerson

//TODO: investigate potential concurrency issues using .use
open class RealmDbManagerImpl(private val appContext: Context) : LocalDbManager {

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"

        const val USER_ID_FIELD = "userId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
    }

    private var realmConfig: RealmConfiguration? = null
    private var localDbKey: LocalDbKey? = null
        set(value) {
            field = value
            value?.let { createAndSaveRealmConfig(value) }
        }

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(localDbKey: LocalDbKey) {
        this.localDbKey = localDbKey
        PeopleRealmEncryptionMigration(localDbKey, appContext)
    }

    override fun insertOrUpdatePersonInLocal(person: rl_Person): Completable =
        insertOrUpdatePeopleInLocal(listOf(person.toDomainPerson()))

    override fun insertOrUpdatePeopleInLocal(people: List<IdPerson>): Completable =
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(people.map(IdPerson::toRealmPerson))
            }
        }
            .ignoreElement()

    override fun getPeopleCountFromLocal(patientId: String?,
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Single<Int> =
        useRealmInstance { realm ->
            realm.buildQueryForPerson(patientId, userId, moduleId, toSync)
                .count()
                .toInt()
        }

    override fun loadPersonFromLocal(personId: String): Single<IdPerson> =
        useRealmInstance { realm ->
            realm.where(rl_Person::class.java).equalTo(PATIENT_ID_FIELD, personId)
                .findFirst()
                ?.toDomainPerson()
                ?: throw IllegalStateException()
        }

    override fun loadPeopleFromLocal(patientId: String?,
                                     userId: String?,
                                     moduleId: String?,
                                     toSync: Boolean?,
                                     sortBy: Map<String, Sort>?): Single<List<IdPerson>> =
        useRealmInstance { realm ->
            realm
                .buildQueryForPerson(patientId, userId, moduleId, toSync, sortBy)
                .findAll()
                .map(rl_Person::toDomainPerson)
        }

    // TODO: improve this terrible usage of RxJava
    override fun loadPeopleFromLocalRx(patientId: String?,
                                       userId: String?,
                                       moduleId: String?,
                                       toSync: Boolean?,
                                       sortBy: Map<String, Sort>?): Flowable<IdPerson> =
        Flowable.create<IdPerson>({ emitter ->
            try {
                useRealmInstance { realm ->
                    realm
                        .buildQueryForPerson(patientId, userId, moduleId, toSync, sortBy)
                        .findAll()
                        .forEach { realmPerson ->
                            emitter.onNext(realmPerson.toDomainPerson())
                        }
                    emitter.onComplete()
                }.blockingGet()
            } catch (t: Throwable) {
                Timber.e(t)
                emitter.onError(t)
            }
        }, BackpressureStrategy.BUFFER)

    override fun loadProjectFromLocal(projectId: String): Single<Project> =
        useRealmInstance { realm ->
            realm
                .where(rl_Project::class.java).equalTo(rl_Project.PROJECT_ID_FIELD, projectId)
                .findFirst()
                ?.let { realm.copyFromRealm(it).toProject() }
                ?: throw NoSuchStoredProjectException()
        }

    override fun deletePeopleFromLocal(syncScope: SyncScope): Completable =
        useRealmInstance { realm ->
            realm.executeTransaction { realmInTransaction ->
                syncScope.toSubSyncScopes().forEach {
                    realmInTransaction.buildQueryForPerson(it)
                        .findAll()
                        .deleteAllFromRealm()
                }
            }
        }.ignoreElement()

    override fun deletePeopleFromLocal(subSyncScope: SubSyncScope): Completable =
        useRealmInstance { realm ->
            realm.executeTransaction { realmInTransaction ->
                realmInTransaction.buildQueryForPerson(subSyncScope)
                    .findAll()
                    .deleteAllFromRealm()
            }
        }.ignoreElement()

    override fun saveProjectIntoLocal(project: Project): Completable =
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(project.toRealmProject())
            }
        }.ignoreElement()

    private fun getRealmConfig(): Single<RealmConfiguration> =
        realmConfig
            ?.let { Single.just(it) }
            ?: throw RealmUninitialisedError("No valid realm Config")

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): Single<RealmConfiguration> =
        Single.just(PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
            .also { realmConfig = it })

    private fun getRealmInstance(): Single<Realm> = getRealmConfig()
        .flatMap {
            Single.just(Realm.getInstance(it))
        }

    private fun <R> useRealmInstance(block: (Realm) -> R): Single<R> =
        getRealmInstance()
            .map { realm ->
                realm.use(block)
            }

    private fun Realm.buildQueryForPerson(patientId: String? = null,
                                          userId: String? = null,
                                          moduleId: String? = null,
                                          toSync: Boolean? = null,
                                          sortBy: Map<String, Sort>? = null): RealmQuery<rl_Person> =
        where(rl_Person::class.java)
            .apply {
                patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
                userId?.let { this.equalTo(USER_ID_FIELD, it) }
                moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
                sortBy?.let { this.sort(sortBy.keys.toTypedArray(), sortBy.values.toTypedArray()) }
            }

    private fun Realm.buildQueryForPerson(subSyncScope: SubSyncScope): RealmQuery<rl_Person> =
        buildQueryForPerson(
            userId = subSyncScope.userId,
            moduleId = subSyncScope.moduleId

        )

    override fun loadPeopleFromLocal(destinationList: MutableList<LibPerson>, group: GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        val result = when (group) {
            GROUP.GLOBAL -> loadPeopleFromLocal()
            GROUP.USER -> loadPeopleFromLocal(userId = userId)
            GROUP.MODULE -> loadPeopleFromLocal(moduleId = moduleId)
        }.blockingGet().map(IdPerson::toLibPerson)
        destinationList.addAll(result)
        callback?.onSuccess(false)
    }

    /**
     *  @Deprecated: do not use it. Use Room DownSyncStatus
     */
    override fun getRlSyncInfo(subSyncScope: SubSyncScope): Single<rl_SyncInfo> =
        useRealmInstance { realm ->
            realm
                .where(rl_SyncInfo::class.java).equalTo(rl_SyncInfo.SYNC_ID_FIELD, subSyncScope.group.ordinal)
                .findFirst()
                ?.let { realm.copyFromRealm(it) }
                ?: throw NoSuchRlSessionInfoException()
        }

    /**
     *  @Deprecated: do not use it. Use Room DownSyncStatus
     */
    override fun deleteSyncInfo(subSyncScope: SubSyncScope): Completable = Completable.fromAction {
        useRealmInstance { realm ->
            realm.where(rl_SyncInfo::class.java).equalTo(rl_SyncInfo.SYNC_ID_FIELD, subSyncScope.group.ordinal)
                .findAll().deleteAllFromRealm()
        }
    }
}
