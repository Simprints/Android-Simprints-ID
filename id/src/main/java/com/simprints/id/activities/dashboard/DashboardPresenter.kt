package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardLocalDbCard
import com.simprints.id.activities.dashboard.models.SyncUIState
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.domain.Constants.GROUP
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.ResourcesHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import java.text.DateFormat
import java.util.*

class DashboardPresenter(private val view: DashboardContract.View,
                         syncClient: SyncClient,
                         val dataManager: DataManager,
                         private val resourcesHelper: ResourcesHelper) : DashboardContract.Presenter {

    private var localDbViewModel: DashboardLocalDbCard? = null

    private val syncManager = SyncManager(dataManager, syncClient, object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            setSyncingProgressInLocalDbCardView(progress)
        }

        override fun onComplete() {
            setSyncingCompleteInLocalDbCardView()
        }

        override fun onError(throwable: Throwable) {
            setSyncingErrorInLocalDbCardView()
        }
    })

    private var started: Boolean = false
    private val dateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    override val cardsModelsList: ArrayList<DashboardCard> = arrayListOf()

    var readableLastTimeSync: String = ""
        get() {
            val lastSyncTime: Date? = dataManager.getSyncInfoFor(dataManager.syncGroup)?.lastSyncTime
                ?: Date() //FIXME
            return if (lastSyncTime != null) dateFormat.format(lastSyncTime).toString() else ""
        }

    override fun start() {
        if (!started) {
            started = true
            initCards()
        }
    }

    private fun initCards() {
        Completable.concatArray(
            createAndAddProjectInfoCard(),
            createAndAddScannerInfoCard(),
            createAndAddLastUserInfoCard(),
            createAndAddLastEnrolInfoCard(),
            createAndAddLastVerificationInfoCard(),
            createAndAddLastIdentificationInfoCard(),
            createAndAddRemoteDbInfoCard()
                .onErrorResumeNext { SingleJust(-1) }
                .flatMapCompletable {
                    createAndAddLocalDbInfoCard(it)
                }
        ).subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { view.stopRequestIfRequired() },
                onError = { view.stopRequestIfRequired() })
    }

    override fun didUserWantToRefreshCards() {
        cardsModelsList.clear()
        initCards()
    }

    override fun pause() {
        syncManager.stop()
    }

    override fun didUserWantToSyncBy(user: GROUP) {
        syncManager.sync(user)
    }

    private fun createAndAddProjectInfoCard(): Completable =
        dataManager
            .loadProject(dataManager.signedInProjectId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                DashboardCard(
                    0,
                    R.drawable.simprints_logo_blue,
                    resourcesHelper.getString(R.string.dashboard_card_project_title),
                    it.description)
                    .also { addCard(it) }
            }.toCompletable()

    private fun createAndAddLocalDbInfoCard(remotePeopleCount: Int): Completable = Completable.fromAction {
        val localPeopleCount = dataManager.getPeopleCount(dataManager.syncGroup)

        localDbViewModel = DashboardLocalDbCard(
            1,
            R.drawable.local_db,
            resourcesHelper.getString(R.string.dashboard_card_localdb_title),
            "$localPeopleCount",
            readableLastTimeSync,
            remotePeopleCount != localPeopleCount,
            {
                didUserWantToSyncBy(dataManager.syncGroup)
            })
            .also { addCard(it) }
    }

    private fun createAndAddRemoteDbInfoCard(): Single<Int> =
        SyncTaskParameters.build(dataManager.syncGroup, dataManager).let {
            dataManager.getNumberOfPatientsForSyncParams(SyncTaskParameters.build(dataManager.syncGroup, dataManager))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    DashboardCard(
                        2,
                        R.drawable.remote_db,
                        resourcesHelper.getString(R.string.dashboard_card_remotedb_title),
                        "$it")
                        .also { addCard(it) }
                }
        }

    private fun createAndAddScannerInfoCard(): Completable = Completable.fromAction {
        if (dataManager.lastScannerUsed.isNotEmpty()) {
            DashboardCard(
                3,
                R.drawable.scanner,
                resourcesHelper.getString(R.string.dashboard_card_scanner_title),
                dataManager.lastScannerUsed)
                .also { addCard(it) }

        }
    }

    private fun createAndAddLastUserInfoCard(): Completable = Completable.fromAction {
        if (dataManager.getSignedInUserIdOrEmpty().isNotEmpty()) {
            DashboardCard(
                4,
                R.drawable.last_user,
                resourcesHelper.getString(R.string.dashboard_card_lastuser_title),
                dataManager.getSignedInUserIdOrEmpty())
                .also { addCard(it) }
        }
    }

    private fun createAndAddLastEnrolInfoCard(): Completable = Completable.fromAction {
        dataManager.lastEnrolDate?.let {
            DashboardCard(
                5,
                R.drawable.fingerprint_enrol,
                resourcesHelper.getString(R.string.dashboard_card_enrol_title),
                dateFormat.format(it).toString())
                .also { addCard(it) }
        }
    }

    private fun createAndAddLastVerificationInfoCard(): Completable = Completable.fromAction {
        dataManager.lastVerificationDate?.let {
            DashboardCard(
                6,
                R.drawable.fingerprint_verification,
                resourcesHelper.getString(R.string.dashboard_card_verification_title),
                dateFormat.format(it).toString())
                .also { addCard(it) }
        }
    }

    private fun createAndAddLastIdentificationInfoCard(): Completable = Completable.fromAction {
        dataManager.lastIdentificationDate?.let {
            DashboardCard(
                7,
                R.drawable.fingerprint_identification,
                resourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
                .also { addCard(it) }
        }
    }

    private fun addCard(dashboardCard: DashboardCard) {
        cardsModelsList.add(dashboardCard)
        cardsModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    private fun setSyncingProgressInLocalDbCardView(progress: Progress) {
        localDbViewModel?.let {
            it.syncState = SyncUIState.IN_PROGRESS
            it.progress = progress
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }

    private fun setSyncingCompleteInLocalDbCardView() {
        localDbViewModel?.apply {
            syncState = SyncUIState.SUCCEED
            lastSyncTime = readableLastTimeSync

            view.notifyCardViewChanged(cardsModelsList.indexOf(this))
        }
    }

    private fun setSyncingErrorInLocalDbCardView() {
        localDbViewModel?.let {
            localDbViewModel?.syncState = SyncUIState.FAILED
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }
}
