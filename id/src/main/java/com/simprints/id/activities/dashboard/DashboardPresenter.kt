package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.activities.dashboard.models.SyncUIState
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.domain.Constants.GROUP
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressService
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

    private var syncViewModel: DashboardSyncCard? = null

    private val syncListener = object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            syncViewModel?.onSyncProgress(progress)
        }

        override fun onComplete() {
            syncViewModel?.onSyncComplete()
        }

        override fun onError(throwable: Throwable) {
            syncViewModel?.onSyncError(throwable)
        }
    }
    private val syncManager = SyncManager(dataManager, syncClient, syncListener)

    private var started: Boolean = false
    private val dateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    override val cardsModelsList: ArrayList<DashboardCard> = arrayListOf()

    private var readableLastTimeSync: String? = null
        get() {
            dataManager.getSyncInfoFor(dataManager.syncGroup)?.lastSyncTime?.let {
                return dateFormat.format(it).toString()
            }

            return null
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
            createAndAddLocalDbInfoCard(),
            createAndAddDbCards()
        ).subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { view.stopRequestIfRequired() },
                onError = { view.stopRequestIfRequired() })
    }

    private fun createAndAddDbCards(): Completable? =
        createAndAddRemoteDbInfoCard()
            .onErrorResumeNext { SingleJust(-1) }
            .flatMapCompletable {
                createAndAddSyncInfoCard(if (dataManager.syncGroup == GROUP.GLOBAL) 93764 else it)
            }


    override fun didUserWantToRefreshCards() {
        if (syncViewModel?.syncState != SyncUIState.IN_PROGRESS) {
            cardsModelsList.clear()
            initCards()
        } else {
            view.stopRequestIfRequired()
        }
    }

    override fun pause() {
        syncManager.stop()
    }

    override fun didUserWantToSyncBy(user: GROUP) {
        setSyncingStartedInLocalDbCardView()
        syncManager.sync(user)
    }

    private fun createAndAddProjectInfoCard(): Completable =
        dataManager
            .loadProject(dataManager.signedInProjectId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                DashboardCard(
                    DashboardCardType.PROJECT_INFO,
                    0,
                    R.drawable.simprints_logo_blue,
                    resourcesHelper.getString(R.string.dashboard_card_project_title),
                    it.description)
                    .also { addCard(it) }
            }.toCompletable()

    private fun createAndAddLocalDbInfoCard(): Completable = Completable.fromAction {
        val localPeopleCount = dataManager.getPeopleCount(dataManager.syncGroup)

        DashboardCard(
            DashboardCardType.LOCAL_DB,
            1,
            R.drawable.local_db,
            resourcesHelper.getString(R.string.dashboard_card_localdb_title),
            "$localPeopleCount")
            .also { addCard(it) }
    }


    private fun createAndAddSyncInfoCard(remotePeopleCount: Int): Completable = Completable.fromAction {
        val peopleToUpload = dataManager.loadPeopleFromLocal(toSync = true).count()

        val syncParams =  SyncTaskParameters.build( dataManager.syncGroup, dataManager)
        val peopleToDownload = dataManager.calculateNPatientsToDownSync(remotePeopleCount, syncParams)

        syncViewModel = DashboardSyncCard(
            DashboardCardType.SYNC_DB,
            2,
            R.drawable.dashboard_sync,
            resourcesHelper.getString(R.string.dashboard_card_localdb_title),
            peopleToUpload,
            peopleToDownload,
            readableLastTimeSync,
            peopleToDownload > 0 || peopleToUpload > 0,
            {
                didUserWantToSyncBy(dataManager.syncGroup)
            })
            .also { addCard(it) }

        //FIXME: Super hacky: if the ProgressService is running, then we try to catch up with its state
        if(ProgressService.isRunning.get()) {
            syncManager.sync(dataManager.syncGroup)
        }
    }

    private fun createAndAddRemoteDbInfoCard(): Single<Int> =
        SyncTaskParameters.build(dataManager.syncGroup, dataManager).let {
            dataManager.getNumberOfPatientsForSyncParams(SyncTaskParameters.build(dataManager.syncGroup, dataManager))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    val total = if (dataManager.syncGroup == GROUP.GLOBAL) 93764 else it
                    DashboardCard(
                        DashboardCardType.REMOTE_DB,
                        3,
                        R.drawable.remote_db,
                        resourcesHelper.getString(R.string.dashboard_card_remotedb_title),
                        "$total")
                        .also { /* addCard(it) */}
                }
        }

    private fun createAndAddScannerInfoCard(): Completable = Completable.fromAction {
        if (dataManager.lastScannerUsed.isNotEmpty()) {
            DashboardCard(
                DashboardCardType.LAST_SCANNER,
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
                DashboardCardType.LAST_USER,
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
                DashboardCardType.LAST_ENROL,
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
                DashboardCardType.LAST_VERIFICATION,
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
                DashboardCardType.LAST_IDENTIFICATION,
                7,
                R.drawable.fingerprint_identification,
                resourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
                .also { addCard(it) }
        }
    }

    private fun addCard(dashboardCard: DashboardCard) {
        removeCardIfExist(dashboardCard.type)

        cardsModelsList.add(dashboardCard)
        cardsModelsList.sortBy { it.position }
        view.updateCardViews()
    }

    private fun setSyncingStartedInLocalDbCardView() {
        syncViewModel?.let {
                it.syncStarted()
                view.notifyCardViewChanged(cardsModelsList.indexOf(it))
            }
    }

    private fun removeCardIfExist(projectType :DashboardCardType) {
        cardsModelsList.findLast { it.type == projectType }.also {
            cardsModelsList.remove(it)
        }
    }
}
