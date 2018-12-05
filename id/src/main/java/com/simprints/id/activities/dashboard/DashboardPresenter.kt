package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.DashboardSyncCardViewModel
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
import javax.inject.Inject
import kotlin.collections.ArrayList

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

    override val cardsViewModelsList: ArrayList<CardViewModel> = arrayListOf()

    init {
        component.inject(this)
    }

    override fun start() {
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        initCards()
    }

    private fun initCards() {
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
        it.viewModelState.onSyncActionClicked = {
            when {
                userIsOffline() -> view.showToastForUserOffline()
                !areThereRecordsToDownSync(it) -> view.showToastForRecordsUpToDate()
                areThereRecordsToDownSync(it) -> userDidWantToDownSync()
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

    private fun areThereRecordsToDownSync(dashboardSyncCardViewModel: DashboardSyncCardViewModel) =
            dashboardSyncCardViewModel.viewModelState.peopleToDownload?.let { it > 0 } ?: false

    private fun cancelAllDownSyncWorkers() {
        syncSchedulerHelper.cancelDownSyncWorkers()
    }
}
