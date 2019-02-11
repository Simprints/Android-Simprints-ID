package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsTags
import com.simprints.id.data.analytics.LogTrigger
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    private val cardsFactory = DashboardCardsFactory(component)

    override val cardsViewModelsList: ArrayList<CardViewModel> = arrayListOf()

    init {
        component.inject(this)
    }

    override fun start() {
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        initCards()
        initOrUpdateAnalyticsKeys()
    }

    private fun initCards() {
        cardsViewModelsList.forEach { it.stopObservers() }
        cardsViewModelsList.clear()
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
        ).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = { handleCardsCreated() },
            onError = {
                handleCardsCreationFailed()
            })
    }

    private fun handleCardsCreated() {
        view.stopRequestIfRequired()
    }

    private fun handleCardsCreationFailed() {
        view.stopRequestIfRequired()
    }

    private fun initSyncCardModel(viewModel: DashboardSyncCardViewModel) {
        viewModel.viewModelState.onSyncActionClicked = {
            analyticsManager.logInfo(AnalyticsTags.SYNC, LogTrigger.UI,"Dashboard card sync button clicked")
            when {
                userIsOffline() -> view.showToastForUserOffline()
                !viewModel.areThereRecordsToSync() -> view.showToastForRecordsUpToDate()
                viewModel.areThereRecordsToSync() -> userDidWantToDownSync()
            }
        }
    }

    private fun addCard(dashboardCard: CardViewModel) {
        removeCardIfExist(dashboardCard.type)

        cardsViewModelsList.add(dashboardCard)
        cardsViewModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    override fun userDidWantToRefreshCardsIfPossible() {
        initCards()
    }

    override fun userDidWantToDownSync() {
        syncSchedulerHelper.startDownSyncOnUserActionIfPossible()
    }

    private fun removeCardIfExist(projectType: DashboardCardType) {
        cardsViewModelsList.findLast { it.type == projectType }.also {
            cardsViewModelsList.remove(it)
        }
    }

    override fun logout() {
        dbManager.signOut()
        syncSchedulerHelper.cancelDownSyncWorkers()
        sessionEventManager.signOut()
    }

    override fun userDidWantToLogout() {
        view.showConfirmationDialogForLogout()
    }

    private fun userIsOffline() = try {
        !simNetworkUtils.isConnected()
    } catch (e: IllegalStateException) {
        true
    }

    private fun initOrUpdateAnalyticsKeys() {
        analyticsManager.setProjectIdCrashlyticsKey(loginInfoManager.getSignedInProjectIdOrEmpty())
        analyticsManager.setUserIdCrashlyticsKey(loginInfoManager.getSignedInUserIdOrEmpty())
        analyticsManager.setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
        analyticsManager.setDownSyncTriggersCrashlyticsKey(preferencesManager.peopleDownSyncTriggers)
        analyticsManager.setFingersSelectedCrashlyticsKey(preferencesManager.fingerStatus)
    }
}
