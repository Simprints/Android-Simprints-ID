package com.simprints.id.data.db.person.local

import android.content.Context
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.data.db.person.local.models.toDomainPerson
import com.simprints.id.data.db.person.local.models.toRealmPerson
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@FlowPreview
class PersonLocalDataSourceImpl(private val appContext: Context,
                                val secureDataManager: SecureDataManager,
                                val loginInfoManager: LoginInfoManager) : PersonLocalDataSource {

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "userId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
    }

    val config: RealmConfiguration by lazy {
        Realm.init(appContext)
        getLocalDbKeyAndCreateRealmConfig()
    }

    private fun getLocalDbKeyAndCreateRealmConfig(): RealmConfiguration =
        loginInfoManager.getSignedInProjectIdOrEmpty().let {
            return if (it.isNotEmpty()) {
                createAndSaveRealmConfig(secureDataManager.getLocalDbKeyOrThrow(it))
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)


    private fun <R> useRealmInstance(block: (Realm) -> R): R =
        Realm.getInstance(config).use(block)

    override fun insertOrUpdate(people: List<Person>) {
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(people.map(Person::toRealmPerson))
            }
        }
    }

    override fun load(query: PersonLocalDataSource.Query): Flow<Person> =
        Realm.getInstance(config).use {
            it.buildQueryForPerson(query)
                .findAll()
                .map { it.toDomainPerson() }
                .asFlow()
        }


    override fun delete(query: PersonLocalDataSource.Query) {
        val realm = Realm.getInstance(config)
        realm.use {
            it.buildQueryForPerson(query)
                .findAll()
                .deleteAllFromRealm()
        }
    }

    override fun count(query: PersonLocalDataSource.Query): Int =
        useRealmInstance { realm ->
            realm.buildQueryForPerson(query).count().toInt()
        }

    private fun Realm.buildQueryForPerson(query: PersonLocalDataSource.Query): RealmQuery<DbPerson> =
        where(DbPerson::class.java)
            .apply {
                query.projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
                query.patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
                query.userId?.let { this.equalTo(USER_ID_FIELD, it) }
                query.moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                query.toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
                query.sortBy?.let { this.sort(it.keys.toTypedArray(), it.values.toTypedArray()) }
            }
}
