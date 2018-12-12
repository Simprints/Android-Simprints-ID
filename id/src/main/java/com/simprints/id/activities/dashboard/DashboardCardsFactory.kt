package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.DashboardCardViewModel
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.tools.utils.AndroidResourcesHelper
import io.reactivex.Single
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class DashboardCardsFactory(private val component: AppComponent) {

    val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    init {
        component.inject(this)
    }

    fun createCards() = arrayListOf(
        createProjectInfoCard(),
        createCurrentUserInfoCard(),
        createSyncInfoCard(),
        createLastScannerInfoCard(),
        createLastEnrolInfoCard(),
        createLastVerificationInfoCard(),
        createLastIdentificationInfoCard()
    ).filterNotNull()

    private fun createProjectInfoCard(position: Int = 0): Single<DashboardCardViewModel> =
        dbManager
            .loadProject(loginInfoManager.getSignedInProjectIdOrEmpty())
            .map {
                DashboardCardViewModel(DashboardCardType.PROJECT_INFO,
                    position,
                    DashboardCardViewModel.State(
                        R.drawable.simprints_logo_blue,
                        it.name,
                        it.description))
            }.doOnError { it.printStackTrace() }

    private fun createCurrentUserInfoCard(position: Int = 1): Single<DashboardCardViewModel>? =
        if (loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()) {
            Single.just(DashboardCardViewModel(DashboardCardType.CURRENT_USER,
                position, DashboardCardViewModel.State(
                R.drawable.current_user,
                getStringFromRes(R.string.dashboard_card_currentuser_title),
                loginInfoManager.getSignedInUserIdOrEmpty()))).doOnError { it.printStackTrace() }
        } else {
            null
        }

    private fun createSyncInfoCard(position: Int = 3): Single<DashboardSyncCardViewModel>? =
        Single.just(DashboardSyncCardViewModel(
            DashboardCardType.SYNC_DB,
            position,
            component,
            syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData(),
            syncStatusDatabase.upSyncDao.getUpSyncStatus()))

    private fun createLastScannerInfoCard(position: Int = 4): Single<DashboardCardViewModel>? {
        return if (preferencesManager.lastScannerUsed.isNotEmpty()) {
            Single.just(DashboardCardViewModel(DashboardCardType.LAST_SCANNER,
                position,
                DashboardCardViewModel.State(
                    R.drawable.scanner,
                    getStringFromRes(R.string.dashboard_card_lastscanner_title),
                    preferencesManager.lastScannerUsed))
            ).doOnError { it.printStackTrace() }
        } else {
            null
        }
    }


    private fun createLastEnrolInfoCard(position: Int = 5): Single<DashboardCardViewModel>? =
        preferencesManager.lastEnrolDate?.let { date ->
            Single.just(DashboardCardViewModel(DashboardCardType.LAST_ENROL,
                position, DashboardCardViewModel.State(
                R.drawable.fingerprint_enrol,
                getStringFromRes(R.string.dashboard_card_enrol_title),
                dateFormat.format(date).toString()))).doOnError { it.printStackTrace() }
        }

    private fun createLastVerificationInfoCard(position: Int = 6): Single<DashboardCardViewModel>? =
        preferencesManager.lastVerificationDate?.let { date ->
            Single.just(DashboardCardViewModel(
                DashboardCardType.LAST_VERIFICATION,
                position, DashboardCardViewModel.State(
                R.drawable.fingerprint_verification,
                getStringFromRes(R.string.dashboard_card_verification_title),
                dateFormat.format(date).toString())
            )).doOnError { it.printStackTrace() }
        }

    private fun createLastIdentificationInfoCard(position: Int = 7): Single<DashboardCardViewModel>? =
        preferencesManager.lastIdentificationDate?.let { date ->
            Single.just(DashboardCardViewModel(
                DashboardCardType.LAST_IDENTIFICATION,
                position,
                DashboardCardViewModel.State(
                    R.drawable.fingerprint_identification,
                    getStringFromRes(R.string.dashboard_card_identification_title),
                    dateFormat.format(date).toString()))).doOnError { it.printStackTrace() }
        }

    private fun getStringFromRes(resId: Int) = androidResourcesHelper.getString(resId)
}
