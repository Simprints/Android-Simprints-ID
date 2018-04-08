package com.simprints.id.activities.dashboard

import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.utils.AndroidResourcesHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.DateFormat
import java.util.*

class DashboardCardsFactory(private val dataManager: DataManager,
                            private val androidResourcesHelper: AndroidResourcesHelper) {

    private val syncParams = {
        SyncTaskParameters.build(dataManager.syncGroup, dataManager)
    }()

    private val dateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun createCards() = arrayListOf(
        createAndAddProjectInfoCard(0),
        createAndAddLocalDbInfoCard(1),
        createAndAddSyncInfoCard(2),
        createAndAddScannerInfoCard(3),
        createAndAddLastUserInfoCard(4),
        createAndAddLastEnrolInfoCard(5),
        createAndAddLastVerificationInfoCard(6),
        createAndAddLastIdentificationInfoCard(7)
        ).filterNotNull()

    private fun createAndAddProjectInfoCard(position: Int): Single<DashboardCard> =
        dataManager
            .loadProject(dataManager.signedInProjectId)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                DashboardCard(
                    DashboardCardType.PROJECT_INFO,
                    position,
                    R.drawable.simprints_logo_blue,
                    androidResourcesHelper.getString(R.string.dashboard_card_project_title),
                    it.description)
            }.doOnError{ print(it.printStackTrace()) }

    private fun createAndAddLocalDbInfoCard(position: Int): Single<DashboardCard> =
        Single.just(
            dataManager.getPeopleCount(dataManager.syncGroup).let {
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
            }
        ).doOnError{ it.printStackTrace() }

    private fun createAndAddSyncInfoCard(position: Int): Single<DashboardSyncCard>? =
        dataManager.getNumberOfPatientsForSyncParams(syncParams)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                DashboardSyncCard(
                    DashboardCardType.SYNC_DB,
                    position,
                    R.drawable.dashboard_sync,
                    androidResourcesHelper.getString(R.string.dashboard_card_sync_title),
                    dataManager,
                    dateFormat,
                    it)
            }.doOnError{ it.printStackTrace() }

    private fun createAndAddScannerInfoCard(position: Int): Single<DashboardCard>? {
        return if (dataManager.lastScannerUsed.isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.LAST_SCANNER,
                position,
                R.drawable.scanner,
                androidResourcesHelper.getString(R.string.dashboard_card_scanner_title),
                dataManager.lastScannerUsed)
            ).doOnError{ it.printStackTrace() }
        } else {
            null
        }
    }

    private fun createAndAddLastUserInfoCard(position: Int): Single<DashboardCard>? {
        return if (dataManager.getSignedInUserIdOrEmpty().isNotEmpty()) {
            Single.just(DashboardCard(
                DashboardCardType.LAST_USER,
                position,
                R.drawable.last_user,
                androidResourcesHelper.getString(R.string.dashboard_card_lastuser_title),
                dataManager.getSignedInUserIdOrEmpty())
            ).doOnError{ it.printStackTrace() }
        } else {
            null
        }
    }

    private fun createAndAddLastEnrolInfoCard(position: Int): Single<DashboardCard>? =
        dataManager.lastEnrolDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_ENROL,
                position,
                R.drawable.fingerprint_enrol,
                androidResourcesHelper.getString(R.string.dashboard_card_enrol_title),
                dateFormat.format(it).toString())
            ).doOnError{ it.printStackTrace() }
        }


    private fun createAndAddLastVerificationInfoCard(position: Int): Single<DashboardCard>? =
        dataManager.lastVerificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_VERIFICATION,
                position,
                R.drawable.fingerprint_verification,
                androidResourcesHelper.getString(R.string.dashboard_card_verification_title),
                dateFormat.format(it).toString())
            ).doOnError{ it.printStackTrace() }
        }

    private fun createAndAddLastIdentificationInfoCard(position: Int): Single<DashboardCard>? =
        dataManager.lastIdentificationDate?.let {
            Single.just(DashboardCard(
                DashboardCardType.LAST_IDENTIFICATION,
                position,
                R.drawable.fingerprint_identification,
                androidResourcesHelper.getString(R.string.dashboard_card_identification_title),
                dateFormat.format(it).toString())
            ).doOnError{ it.printStackTrace() }
        }
}
