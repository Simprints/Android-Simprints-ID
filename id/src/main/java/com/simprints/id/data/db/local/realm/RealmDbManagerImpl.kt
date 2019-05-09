package com.simprints.id.data.db.local.realm

import android.content.Context
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.*
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.Project
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.data.db.NoSuchDbSyncInfoException
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
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

//TODO: investigate potential concurrency issues using .use
open class RealmDbManagerImpl(appContext: Context,
                              val secureDataManager: SecureDataManager,
                              val loginInfoManager: LoginInfoManager) : LocalDbManager {

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "userId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
    }

    private var realmConfig: RealmConfiguration? = null

    init {
        Realm.init(appContext) //TODO: Avoid doing stuff in init, chain with createAndSaveRealmConfig for easier tests
    }

    override fun insertOrUpdatePersonInLocal(person: Person): Completable =
        insertOrUpdatePeopleInLocal(listOf(person))

    override fun insertOrUpdatePeopleInLocal(people: List<Person>): Completable =
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(people.map(Person::toRealmPerson))
            }
        }
            .ignoreElement()

    override fun getPeopleCountFromLocal(projectId: String?,
                                         patientId: String?,
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Single<Int> =
        useRealmInstance { realm ->
            realm.buildQueryForPerson(projectId, patientId, userId, moduleId, toSync)
                .count()
                .toInt()
        }

    override fun loadPersonFromLocal(personId: String): Single<Person> =
        useRealmInstance { realm ->
            realm.where(DbPerson::class.java).equalTo(PATIENT_ID_FIELD, personId)
                .findFirst()
                ?.toDomainPerson()
                ?: throw IllegalStateException()
        }

    override fun loadPeopleFromLocal(projectId: String?,
                                     patientId: String?,
                                     userId: String?,
                                     moduleId: String?,
                                     toSync: Boolean?,
                                     sortBy: Map<String, Sort>?): Single<List<Person>> =
        useRealmInstance { realm ->
            realm
                .buildQueryForPerson(projectId, patientId, userId, moduleId, toSync, sortBy)
                .findAll()
                .map(DbPerson::toDomainPerson)
        }

    // TODO: improve this terrible usage of RxJava
    override fun loadPeopleFromLocalRx(projectId: String?,
                                       patientId: String?,
                                       userId: String?,
                                       moduleId: String?,
                                       toSync: Boolean?,
                                       sortBy: Map<String, Sort>?): Flowable<Person> =
        Flowable.create<Person>({ emitter ->
            try {
                useRealmInstance { realm ->
                    realm
                        .buildQueryForPerson(projectId, patientId, userId, moduleId, toSync, sortBy)
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
                .where(DbProject::class.java).equalTo(DbProject.PROJECT_ID_FIELD, projectId)
                .findFirst()
                ?.let { realm.copyFromRealm(it).toDomainProject() }
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
            ?: getLocalDbKeyAndCreateRealmConfig()

    private fun getLocalDbKeyAndCreateRealmConfig() =
        loginInfoManager.getSignedInProjectIdOrEmpty().let {
            if (it.isNotEmpty()) {
                createAndSaveRealmConfig(secureDataManager.getLocalDbKeyOrThrow(it))
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }

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

    private fun Realm.buildQueryForPerson(projectId: String? = null,
                                          patientId: String? = null,
                                          userId: String? = null,
                                          moduleId: String? = null,
                                          toSync: Boolean? = null,
                                          sortBy: Map<String, Sort>? = null): RealmQuery<DbPerson> =
        where(DbPerson::class.java)
            .apply {
                projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
                patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
                userId?.let { this.equalTo(USER_ID_FIELD, it) }
                moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
                sortBy?.let { this.sort(sortBy.keys.toTypedArray(), sortBy.values.toTypedArray()) }
            }

    private fun Realm.buildQueryForPerson(subSyncScope: SubSyncScope): RealmQuery<DbPerson> =
        buildQueryForPerson(
            userId = subSyncScope.userId,
            moduleId = subSyncScope.moduleId

        )

    /**
     *  @Deprecated: do not use it. Use Room DownSyncStatus
     */
    override fun getDbSyncInfo(subSyncScope: SubSyncScope): Single<DbSyncInfo> =
        useRealmInstance { realm ->
            realm
                .where(DbSyncInfo::class.java).equalTo(DbSyncInfo.SYNC_ID_FIELD, subSyncScope.group.ordinal)
                .findFirst()
                ?.let { realm.copyFromRealm(it) }
                ?: throw NoSuchDbSyncInfoException()
        }

    /**
     *  @Deprecated: do not use it. Use Room DownSyncStatus
     */
    override fun deleteSyncInfo(subSyncScope: SubSyncScope): Completable = Completable.fromAction {
        useRealmInstance { realm ->
            realm.where(DbSyncInfo::class.java).equalTo(DbSyncInfo.SYNC_ID_FIELD, subSyncScope.group.ordinal)
                .findAll().deleteAllFromRealm()
        }
    }
}
