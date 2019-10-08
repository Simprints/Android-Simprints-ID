package com.simprints.id.data.db.person.local

import android.content.Context
import com.simprints.id.data.db.common.realm.PeopleRealmConfig
import com.simprints.id.data.db.person.domain.FingerprintRecord
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.data.db.person.local.models.fromDbToDomain
import com.simprints.id.data.db.person.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.tools.extensions.await
import com.simprints.id.tools.extensions.transactAwait
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.Serializable

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


    override suspend fun insertOrUpdate(people: List<Person>) {
        withContext(Dispatchers.Main) {
            Realm.getInstance(config).use { realm ->
                realm.transactAwait {
                    it.insertOrUpdate(people.map(Person::fromDomainToDb))
                }
            }
        }
    }

    override suspend fun load(query: PersonLocalDataSource.Query?): Flow<Person> =
        withContext(Dispatchers.Main) {
            Realm.getInstance(config).use {
                it.buildQueryForPerson(query)
                    .await()
                    ?.map { it.fromDbToDomain() }
                    ?.asFlow()
                    ?: flowOf()
            }
        }

    override suspend fun loadFingerprintRecords(query: Serializable): Flow<FingerprintRecord> =
        if (query is PersonLocalDataSource.Query) {
            load(query).map { person ->
                createFingerprintRecordFlow(person)
            }.flattenConcat()
        } else {
            throw InvalidQueryToLoadRecordsException()
        }

    private fun createFingerprintRecordFlow(person: Person): Flow<FingerprintRecord> =
        person.fingerprintSamples.map {
            FingerprintRecord(person.patientId, it)
        }.asFlow()

    override suspend fun delete(query: PersonLocalDataSource.Query) {
        withContext(Dispatchers.Main) {
            Realm.getInstance(config).use {
                it.buildQueryForPerson(query)
                    .await()?.deleteAllFromRealm()
            }
        }
    }

    override fun count(query: PersonLocalDataSource.Query): Int =
        Realm.getInstance(config).use { realm ->
            realm.buildQueryForPerson(query).count().toInt()
        }

    private fun Realm.buildQueryForPerson(query: PersonLocalDataSource.Query?): RealmQuery<DbPerson> =
        where(DbPerson::class.java)
            .apply {
                query?.let { query ->
                    query.projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
                    query.patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
                    query.userId?.let { this.equalTo(USER_ID_FIELD, it) }
                    query.moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                    query.toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
                    query.sortBy?.let { this.sort(it.keys.toTypedArray(), it.values.toTypedArray()) }
                }
            }
}
