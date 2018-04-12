package com.simprints.id.activities.dashboard

import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.db.sync.model.SyncManagerState
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.utils.AndroidResourcesHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class DashboardPresenter(private val view: DashboardContract.View,
                         syncClient: SyncClient,
                         val dataManager: DataManager,
                         private val androidResourcesHelper: AndroidResourcesHelper) : DashboardContract.Presenter {

    private var started: AtomicBoolean = AtomicBoolean(false)

    private val syncManager = SyncManager(dataManager, syncClient)

    private var actualSyncParams: SyncTaskParameters =
        SyncTaskParameters.build(dataManager.syncGroup, dataManager)

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
        val syncParams = SyncTaskParameters.build(dataManager.syncGroup, dataManager)
        return (actualSyncParams != syncParams).also {
            actualSyncParams = syncParams
        }
    }

    override fun pause() {
        syncManager.stopListeners()
    }

    private fun initCards() {
        val cardsFactory = DashboardCardsFactory(dataManager, androidResourcesHelper)
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
        it.onSyncActionClicked = { didUserWantToSync() }
        syncManager.addObserver(it.syncObserver)
        syncManager.addObserver(object : DisposableObserver<Progress>() {
            override fun onNext(t: Progress) {}
            override fun onError(e: Throwable) { e.printStackTrace() }
            override fun onComplete() {}
        })
    }

    private fun addCard(dashboardCard: DashboardCard) {
        removeCardIfExist(dashboardCard.type)

        cardsModelsList.add(dashboardCard)
        cardsModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    private fun catchUpWithSyncStateIfServiceRunning() {
        if (ProgressService.isRunning.get()) {
            syncManager.sync(SyncTaskParameters.build(dataManager.syncGroup, dataManager))
        }
    }

    override fun didUserWantToRefreshCardsIfPossible() {
        if (isUserAllowedToRefresh()) {
            initCards()
        } else {
            view.stopRequestIfRequired()
        }
    }

    private fun isUserAllowedToRefresh(): Boolean = syncCardModel?.syncState != SyncManagerState.IN_PROGRESS

    override fun didUserWantToSync() {
        setSyncingStartedInLocalDbCardView()
        syncManager.sync(SyncTaskParameters.build(dataManager.syncGroup, dataManager))
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
}
