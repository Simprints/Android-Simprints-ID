package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCardViewModel
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.tools.utils.SimNetworkUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.doAsync
import java.util.*
import javax.inject.Inject

class DashboardPresenter(private val view: DashboardContract.View,
                         val component: AppComponent) : DashboardContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    @Inject lateinit var sessionEventManager: SessionEventsManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    private val cardsFactory = DashboardCardsFactory(view.getLifeCycleOwner(), component)

    override val cardsModelsList: ArrayList<DashboardCard> = arrayListOf()

    init {
        component.inject(this)
    }

    override fun start() {
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        initCards()
    }

    private fun initCards() {
        cardsModelsList.clear()
        Single.merge(
            cardsFactory.createCards()
                .map {
                    it.doOnSuccess { dashboardCard ->
                        if (dashboardCard is DashboardSyncCardViewModel) {
                            initSyncCardModel(dashboardCard)
                        }
                        addCard(dashboardCard)
                    }
                }
        )
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = { handleCardsCreated() },
            onError = { handleCardsCreationFailed() })
    }

    private fun handleCardsCreated() {
        view.stopRequestIfRequired()
    }

    private fun handleCardsCreationFailed() {
        view.stopRequestIfRequired()
    }

    private fun initSyncCardModel(it: DashboardSyncCardViewModel) {
        it.onSyncActionClicked = {
            when {
                userIsOffline() -> view.showToastForUserOffline()
                !areThereRecordsToSync(it) -> view.showToastForRecordsUpToDate()
                areThereRecordsToSync(it) -> userDidWantToSync()
            }
        }
    }

    private fun addCard(dashboardCard: DashboardCard) {
        removeCardIfExist(dashboardCard.type)

        cardsModelsList.add(dashboardCard)
        cardsModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    override fun userDidWantToRefreshCardsIfPossible() {
        initCards()
    }

    override fun userDidWantToSync() {
        syncSchedulerHelper.startDownSyncOnUserActionIfPossible()
    }

    private fun removeCardIfExist(projectType: DashboardCardType) {
        cardsModelsList.findLast { it.type == projectType }.also {
            cardsModelsList.remove(it)
        }
    }

    override fun logout() {
        // STOPSHIP : please god remove this
        syncScopeBuilder.buildSyncScope()?.let {
            dbManager.local.deletePeopleFromLocal(it).blockingAwait()
            doAsync {
                syncStatusDatabase.downSyncDao.lolDelete()
            }
        }

//        dbManager.signOut()
//        cancelAllDownSyncWorkers()
//        sessionEventManager.signOut()
    }

    override fun userDidWantToLogout() {
        view.showConfirmationDialogForLogout()
    }

    private fun userIsOffline() = try {
        !simNetworkUtils.isConnected()
    } catch (e: IllegalStateException) {
        true
    }

    private fun areThereRecordsToSync(dashboardSyncCardViewModel: DashboardSyncCardViewModel) = true //StopShip
        //dashboardSyncCardViewModel.peopleToUpload > 0 || dashboardSyncCardViewModel.peopleToDownload > 0

    private fun cancelAllDownSyncWorkers() {
        syncSchedulerHelper.cancelDownSyncWorkers()
    }
}
