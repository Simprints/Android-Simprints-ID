package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardLocalDbCard
import com.simprints.id.activities.dashboard.models.SyncUIState
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.NaiveSyncManager
import com.simprints.id.domain.Constants.GROUP
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.ResourcesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.util.*

class DashboardPresenter(private val view: DashboardContract.View,
                         syncClient: SyncClient,
                         val dataManager: DataManager,
                         private val resourcesHelper: ResourcesHelper) : DashboardContract.Presenter {

    private var localDbViewModel: DashboardLocalDbCard? = null

    private val syncManager = NaiveSyncManager(dataManager, syncClient, object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            setSyncingProgressInLocalDbCardView(progress)
            if (localDbViewModel?.syncState != SyncUIState.IN_PROGRESS) {
                localDbViewModel?.syncState = SyncUIState.IN_PROGRESS
            }
        }

        override fun onComplete() {
            setSyncingCompleteInLocalDbCardView()
            localDbViewModel?.syncState = SyncUIState.SUCCEED
        }

        override fun onError(throwable: Throwable) {
            setSyncingErrorInLocalDbCardView()
            localDbViewModel?.syncState = SyncUIState.FAILED
        }
    })

    private var started: Boolean = false
    private val dateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    override val cardsModelsList: ArrayList<DashboardCard> = arrayListOf()

    override fun start() {
        if (!started) {
            started = true
            initCards()
        }
    }

    private fun initCards() {
        createAndAddProjectInfoCard()
        createAndAddLocalDbInfoCard()
        createAndAddRemoteDbInfoCard()
        createAndAddScannerInfoCard()
        createAndAddLastEnrolInfoCard()
        createAndAddLastVerificationInfoCard()
        createAndAddLastIdentificationInfoCard()
    }

    override fun pause() {
        syncManager.stop()
    }

    override fun didUserWantToSyncBy(user: GROUP) {
        syncManager.sync(user)
    }

    private fun createAndAddProjectInfoCard() {
        dataManager
            .loadProject(dataManager.signedInProjectId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {

                    val cardModel = DashboardCard(
                        R.drawable.simprints_logo_blue,
                        resourcesHelper.getString(R.string.dashboard_card_project_title),
                        it.description)

                    addCard(cardModel, Math.min(0, cardsModelsList.size))
                },
                onError = { e -> e.printStackTrace() })
    }

    private fun createAndAddLocalDbInfoCard() {
        val count = dataManager.getPeopleCount(dataManager.syncGroup)
        val localDbCard = DashboardLocalDbCard(
            R.drawable.local_db,
            resourcesHelper.getString(R.string.dashboard_card_localdb_title),
            "$count") {
            didUserWantToSyncBy(dataManager.syncGroup)
        }
        localDbViewModel = localDbCard
        addCard(localDbCard, Math.min(1, cardsModelsList.size))
    }

    private fun createAndAddRemoteDbInfoCard() {
        val syncParams = SyncTaskParameters.build(dataManager.syncGroup, dataManager)
        dataManager.getNumberOfPatientsForSyncParams(syncParams).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {

                    val cardModel = DashboardCard(
                        R.drawable.remote_db,
                        resourcesHelper.getString(R.string.dashboard_card_remotedb_title),
                        "$it")

                    addCard(cardModel, Math.min(2, cardsModelsList.size))
                },
                onError = { e -> e.printStackTrace() })
    }

    private fun createAndAddScannerInfoCard() {
        if (dataManager.lastScannerUsed.isNotEmpty()) {
            val cardModel = DashboardCard(
                R.drawable.scanner,
                resourcesHelper.getString(R.string.dashboard_card_scanner_title),
                dataManager.lastScannerUsed)

            addCard(cardModel, Math.min(3, cardsModelsList.size))
        }
    }

    private fun createAndAddLastEnrolInfoCard() {
        dataManager.lastEnrolDate?.let {
            val cardModel = DashboardCard(
                R.drawable.fingerprint_enrol,
                resourcesHelper.getString(R.string.dashboard_card_enrol_title),
                dateFormat.format(it).toString())

            addCard(cardModel, Math.min(4, cardsModelsList.size))
        }
    }

    private fun createAndAddLastVerificationInfoCard() {
        dataManager.lastVerificationDate?.let {
            val cardModel = DashboardCard(
                R.drawable.fingerprint_verification,
                resourcesHelper.getString(R.string.dashboard_card_verification_title),
                dateFormat.format(it).toString())

            addCard(cardModel, Math.min(5, cardsModelsList.size))
        }
    }

    private fun createAndAddLastIdentificationInfoCard() {
        dataManager.lastIdentificationDate?.let {
            val cardModel = DashboardCard(
                R.drawable.fingerprint_identification,
                resourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
            addCard(cardModel, Math.min(6, cardsModelsList.size))
        }
    }

    private fun addCard(dashboardCard: DashboardCard, index: Int?) {
        cardsModelsList.add(index ?: cardsModelsList.size, dashboardCard)
        view.updateCardViews()
    }

    private fun setSyncingProgressInLocalDbCardView(progress: Progress) {
        localDbViewModel?.let {
            localDbViewModel?.syncState = SyncUIState.IN_PROGRESS
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }

    private fun setSyncingCompleteInLocalDbCardView() {
        localDbViewModel?.let {
            localDbViewModel?.syncState = SyncUIState.SUCCEED
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }

    private fun setSyncingErrorInLocalDbCardView() {
        localDbViewModel?.let {
            localDbViewModel?.syncState = SyncUIState.FAILED
            view.notifyCardViewChanged(cardsModelsList.indexOf(it))
        }
    }
}
