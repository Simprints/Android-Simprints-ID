package com.simprints.id.activities.dashboard.models

import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.rxkotlin.subscribeBy
import java.text.DateFormat
import javax.inject.Inject

class DashboardSyncCard(component: AppComponent,
                        type: DashboardCardType,
                        position: Int,
                        imageRes: Int,
                        title: String,
                        private val dateFormat: DateFormat) : DashboardCard(type, position, imageRes, title, "") {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager)
    }

    var onSyncActionClicked: (cardModel: DashboardSyncCard) -> Unit = {}
    var peopleToUpload: Int = 0
    var peopleToDownload: Int = 0
    var peopleInDb: Int = 0
    var syncNeeded: Boolean = false
    var cardView: DashboardSyncCardView? = null

    init {
        component.inject(this)
        updateSyncInfo()
    }

    fun updateSyncInfo() {
        updateTotalLocalPeopleCount()
        updateLocalPeopleToUpSyncCount()
    }

    private fun updateTotalLocalPeopleCount() {
        dbManager.getPeopleCount(preferencesManager.syncGroup).subscribeBy(
            onSuccess = {
                peopleInDb = it
                cardView?.updateCard(this)
            },
            onError = { it.printStackTrace() })
    }

    private fun updateLocalPeopleToUpSyncCount() {
        localDbManager
                .getPeopleCountFromLocal(toSync = true)
            .subscribeBy(
                onSuccess = {
                    peopleToUpload = it
                    cardView?.updateCard(this)
                },
                onError = { it.printStackTrace() })
    }
}
