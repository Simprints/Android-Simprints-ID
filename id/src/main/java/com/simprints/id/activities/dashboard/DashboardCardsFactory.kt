package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.tools.utils.AndroidResourcesHelper
import io.reactivex.Single
import java.text.DateFormat
import java.util.*

class DashboardCardsFactory(private val dataManager: DataManager,
                            private val androidResourcesHelper: AndroidResourcesHelper) {

    val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun createCards() = arrayListOf(
        createProjectInfoCard(),
        createLocalDbInfoCard(),
        createSyncInfoCard(),
        createLastScannerInfoCard(),
        createLastUserInfoCard(),
        createLastEnrolInfoCard(),
        createLastVerificationInfoCard(),
        createLastIdentificationInfoCard()
        ).filterNotNull()

    private fun createProjectInfoCard(position: Int = 0): Single<DashboardCard> =
        dataManager
            .loadProject(dataManager.getSignedInProjectIdOrEmpty())
            .map {
                DashboardCard(
                    DashboardCardType.PROJECT_INFO,
                    position,
                    R.drawable.simprints_logo_blue,
                    it.name,
                    it.description)
            }.doOnError { it.printStackTrace() }

    fun createLocalDbInfoCard(position: Int = 1): Single<DashboardCard> =
        dataManager.getPeopleCount(dataManager.syncGroup).map {
                val titleRes =
                    if (dataManager.syncGroup == Constants.GROUP.USER) {
                        R.string.dashboard_card_localdb_sync_user_title
                    } else {
                        R.string.dashboard_card_localdb_sync_project_title
                    }

                DashboardCard(
                    DashboardCardType.LOCAL_DB,
                    position,
                    R.drawable.local_db,
                    androidResourcesHelper.getString(titleRes),
                    "$it")
            }.doOnError { it.printStackTrace() }

    private fun createSyncInfoCard(position: Int = 2): Single<DashboardSyncCard>? =
        Single.just(
            DashboardSyncCard(
                DashboardCardType.SYNC_DB,
                position,
                R.drawable.dashboard_sync,
                androidResourcesHelper.getString(R.string.dashboard_card_sync_title),
                dataManager,
                dateFormat)
        )

    private fun createLastScannerInfoCard(position: Int = 3): Single<DashboardCard>? {
        return if (dataManager.lastScannerUsed.isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.LAST_SCANNER,
                position,
                R.drawable.scanner,
                androidResourcesHelper.getString(R.string.dashboard_card_lastscanner_title),
                dataManager.lastScannerUsed)
            ).doOnError { it.printStackTrace() }
        } else {
            null
        }
    }

    //TODO: think about the option to use Maybe
    private fun createLastUserInfoCard(position: Int = 4): Single<DashboardCard>? {
        return if (dataManager.lastUserUsed.isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.LAST_USER,
                position,
                R.drawable.last_user,
                androidResourcesHelper.getString(R.string.dashboard_card_lastuser_title),
                dataManager.lastUserUsed)
            ).doOnError { it.printStackTrace() }
        } else {
            null
        }
    }

    private fun createLastEnrolInfoCard(position: Int = 5): Single<DashboardCard>? =
        dataManager.lastEnrolDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_ENROL,
                position,
                R.drawable.fingerprint_enrol,
                androidResourcesHelper.getString(R.string.dashboard_card_enrol_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }

    private fun createLastVerificationInfoCard(position: Int = 6): Single<DashboardCard>? =
        dataManager.lastVerificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_VERIFICATION,
                position,
                R.drawable.fingerprint_verification,
                androidResourcesHelper.getString(R.string.dashboard_card_verification_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }

    private fun createLastIdentificationInfoCard(position: Int = 7): Single<DashboardCard>? =
        dataManager.lastIdentificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_IDENTIFICATION,
                position,
                R.drawable.fingerprint_identification,
                androidResourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
            ).doOnError { it.printStackTrace() }
        }
}
