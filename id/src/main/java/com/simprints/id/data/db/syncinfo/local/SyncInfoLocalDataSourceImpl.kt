package com.simprints.id.data.db.syncinfo.local

import android.content.Context
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.syncinfo.local.models.DbSyncInfo
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.data.db.NoSuchDbSyncInfoException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.realm.Realm
import io.realm.RealmConfiguration

class SyncInfoLocalDataSourceImpl(private val appContext: Context,
                                  val secureDataManager: SecureDataManager,
                                  val loginInfoManager: LoginInfoManager) : SyncInfoLocalDataSource {

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

    /**
     *  @Deprecated: do not use it. Use Room DownSyncStatus
     */
    override fun load(subSyncScope: SubSyncScope): DbSyncInfo =
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
    override fun delete(subSyncScope: SubSyncScope) {
        useRealmInstance { realm ->
            realm.where(DbSyncInfo::class.java).equalTo(DbSyncInfo.SYNC_ID_FIELD, subSyncScope.group.ordinal)
                .findAll().deleteAllFromRealm()
        }
    }
}
