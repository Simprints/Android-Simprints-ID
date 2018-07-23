package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.Constants
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

    init {
        component.inject(this)
    }

    fun createCards() = arrayListOf(
        createProjectInfoCard(),
        createCurrentUserInfoCard(),
        createLocalDbInfoCard(),
        createSyncInfoCard(),
        createLastScannerInfoCard(),
        createLastEnrolInfoCard(),
        createLastVerificationInfoCard(),
        createLastIdentificationInfoCard()
    ).filterNotNull()

    private fun createProjectInfoCard(position: Int = 0): Single<DashboardCard> =
        dbManager
            .loadProject(loginInfoManager.getSignedInProjectIdOrEmpty())
            .map {
                DashboardCard(
                    DashboardCardType.PROJECT_INFO,
                    position,
                    R.drawable.simprints_logo_blue,
                    it.name,
                    it.description)
            }.doOnError { it.printStackTrace() }

    private fun createCurrentUserInfoCard(position: Int = 1): Single<DashboardCard>? =
        if (loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.CURRENT_USER,
                position,
                R.drawable.current_user,
                androidResourcesHelper.getString(R.string.dashboard_card_currentuser_title),
                loginInfoManager.getSignedInUserIdOrEmpty())
            ).doOnError { it.printStackTrace() }
        } else {
            null
        }

    fun createLocalDbInfoCard(position: Int = 2): Single<DashboardCard> =
        dbManager.getPeopleCount(preferencesManager.syncGroup).map { numberOfPeople ->
            DashboardCard(
                DashboardCardType.LOCAL_DB,
                position,
                R.drawable.local_db,
                getLocalDbInfoTitle(),
                "$numberOfPeople")
        }.doOnError { it.printStackTrace() }

    private fun getLocalDbInfoTitle(): String =
        androidResourcesHelper.getString(
            if (preferencesManager.syncGroup == Constants.GROUP.USER)
                R.string.dashboard_card_localdb_sync_user_title
            else
                R.string.dashboard_card_localdb_sync_project_title)

    private fun createSyncInfoCard(position: Int = 3): Single<DashboardSyncCard>? =
        Single.just(
            DashboardSyncCard(
                component,
                DashboardCardType.SYNC_DB,
                position,
                R.drawable.dashboard_sync,
                androidResourcesHelper.getString(R.string.dashboard_card_sync_title),
                dateFormat))

    private fun createLastScannerInfoCard(position: Int = 4): Single<DashboardCard>? {
        return if (preferencesManager.lastScannerUsed.isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.LAST_SCANNER,
                position,
                R.drawable.scanner,
                androidResourcesHelper.getString(R.string.dashboard_card_lastscanner_title),
                preferencesManager.lastScannerUsed)
            ).doOnError { it.printStackTrace() }
        } else {
            null
        }
    }

    private fun createLastEnrolInfoCard(position: Int = 5): Single<DashboardCard>? =
        preferencesManager.lastEnrolDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_ENROL,
                position,
                R.drawable.fingerprint_enrol,
                androidResourcesHelper.getString(R.string.dashboard_card_enrol_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }

    private fun createLastVerificationInfoCard(position: Int = 6): Single<DashboardCard>? =
        preferencesManager.lastVerificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_VERIFICATION,
                position,
                R.drawable.fingerprint_verification,
                androidResourcesHelper.getString(R.string.dashboard_card_verification_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }

    private fun createLastIdentificationInfoCard(position: Int = 7): Single<DashboardCard>? =
        preferencesManager.lastIdentificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_IDENTIFICATION,
                position,
                R.drawable.fingerprint_identification,
                androidResourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }
}
