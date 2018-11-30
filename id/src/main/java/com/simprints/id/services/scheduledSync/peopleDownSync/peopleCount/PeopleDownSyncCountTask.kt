package com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Single

class PeopleDownSyncCountTask(
    private val remoteDbManager: RemoteDbManager,
    private val dbManager: DbManager,
    private val preferencesManager: PreferencesManager,
    private val loginInfoManager: LoginInfoManager) {

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.selectedModules, loginInfoManager)
    }

    fun execute(): Single<Int> =
        remoteDbManager.getNumberOfPatientsForSyncParams(syncParams)
            .flatMap {
                dbManager.calculateNPatientsToDownSyncForSyncParams(it, syncParams)
            }
}
