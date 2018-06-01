package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.db.sync.models.SyncManagerState
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.utils.AndroidResourcesHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class DashboardPresenter(private val view: DashboardContract.View,
                         syncClient: SyncClient,
                         val component: AppComponent,
                         androidResourcesHelper: AndroidResourcesHelper) : DashboardContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager

    private var started: AtomicBoolean = AtomicBoolean(false)

    private val syncManager by lazy {
        SyncManager(analyticsManager, syncClient)
    }

    private val cardsFactory = DashboardCardsFactory(component, androidResourcesHelper)

    private var actualSyncParams: SyncTaskParameters by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager)
    }

    override val cardsModelsList: ArrayList<DashboardCard> = arrayListOf()

    private var syncCardModel: DashboardSyncCard? = null
        get() {
            return cardsModelsList.first { it is DashboardSyncCard } as DashboardSyncCard?
        }

    override fun start() {

        if (!started.getAndSet(true) || hasSyncGroupChangedSinceLastRun()) {
            initCards()
        } else {
            catchUpWithSyncStateIfServiceRunning()
        }
    }

    private fun hasSyncGroupChangedSinceLastRun(): Boolean {
        val syncParams = SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager)
        return (actualSyncParams != syncParams).also {
            actualSyncParams = syncParams
        }
    }

    override fun pause() {
        syncManager.stopListeners()
    }

    private fun initCards() {
        cardsModelsList.clear()
        syncManager.removeObservers()

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
        catchUpWithSyncStateIfServiceRunning()
        view.stopRequestIfRequired()
    }

    private fun handleCardsCreationFailed() {
        view.stopRequestIfRequired()
    }

    private fun initSyncCardModel(it: DashboardSyncCard) {
        it.onSyncActionClicked = { userDidWantToSync() }
        syncManager.addObserver(it.syncObserver)
        syncManager.addObserver(object : DisposableObserver<Progress>() {
            override fun onNext(t: Progress) {}
            override fun onError(e: Throwable) {
                e.printStackTrace()
                createAndAddLocalDbCard()
            }

            override fun onComplete() {
                createAndAddLocalDbCard()
            }
        })
    }

    fun createAndAddLocalDbCard() {
        cardsFactory.createLocalDbInfoCard()
            .subscribeBy(
                onSuccess = { addCard(it) },
                onError = { it.printStackTrace() })
    }

    private fun addCard(dashboardCard: DashboardCard) {
        removeCardIfExist(dashboardCard.type)

        cardsModelsList.add(dashboardCard)
        cardsModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    private fun catchUpWithSyncStateIfServiceRunning() {
        if (ProgressService.isRunning.get()) {
            // The "sync" happens only once at time on Service, no matters how many times we call "sync".
            // When "sync" is called, syncManager connect to the Service and syncManager either starts
            // the sync or catch with the Sync state.
            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager))
        }
    }

    override fun userDidWantToRefreshCardsIfPossible() {
        if (isUserAllowedToRefresh()) {
            initCards()
        } else {
            view.stopRequestIfRequired()
        }
    }

    private fun isUserAllowedToRefresh(): Boolean = syncCardModel?.syncState != SyncManagerState.IN_PROGRESS

    override fun userDidWantToSync() {
        setSyncingStartedInLocalDbCardView()
        syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager))
    }

    private fun setSyncingStartedInLocalDbCardView() {
        syncCardModel?.let {
            it.syncStarted()
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }

    private fun removeCardIfExist(projectType: DashboardCardType) {
        cardsModelsList.findLast { it.type == projectType }.also {
            cardsModelsList.remove(it)
        }
    }

    override fun logout() {
        loginInfoManager.cleanCredentials()
        dbManager.signOut()
    }
}
