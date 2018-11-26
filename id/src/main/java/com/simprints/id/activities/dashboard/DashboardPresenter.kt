package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncState
import com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount.OneTimeDownSyncCountMaster
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import javax.inject.Inject

class DashboardPresenter(private val view: DashboardContract.View,
                         val component: AppComponent) : DashboardContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher
    @Inject lateinit var oneTimeDownSyncCountMaster: OneTimeDownSyncCountMaster
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster

    private val cardsFactory = DashboardCardsFactory(component)

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
        oneTimeDownSyncCountMaster.schedule(preferencesManager.projectId)
        Single.merge(
            cardsFactory.createCards()
                .map {
                    it.doOnSuccess {
                        if (it is DashboardSyncCard) {
                            initSyncCardModel(it)
                        }
                        addCard(it)
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

    private fun initSyncCardModel(it: DashboardSyncCard) {
        it.onSyncActionClicked = {
            if (it.peopleToUpload > 0 || it.peopleToDownload > 0 ) {
                userDidWantToSync()
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
        if (preferencesManager.peopleDownSyncState != PeopleDownSyncState.OFF) {
            peopleDownSyncMaster.schedule(preferencesManager.projectId)
        }
    }

    private fun removeCardIfExist(projectType: DashboardCardType) {
        cardsModelsList.findLast { it.type == projectType }.also {
            cardsModelsList.remove(it)
        }
    }

    override fun logout() {
        dbManager.signOut()
    }

    override fun userDidWantToLogout() {
        view.showConfirmationDialogForLogout()
    }
}
