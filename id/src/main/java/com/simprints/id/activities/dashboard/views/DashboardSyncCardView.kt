package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.db.sync.models.SyncManagerState
import com.simprints.id.data.db.sync.room.SyncStatus
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.db.sync.viewModel.SyncStatusViewModel
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncCard: CardView = rootView.findViewById(R.id.dashboardCardSync)

    private val syncStateIcon: ImageView = rootView.findViewById(R.id.dashboardCardSyncState)
    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncAction: TextView = rootView.findViewById(R.id.dashboardCardSyncAction)

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun updateCard(cardModel: DashboardCard) {
        bind(cardModel)
    }

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        val component = (rootView.context.applicationContext as Application).component
        component.inject(this)

        if (cardModel is DashboardSyncCard) {
            cardModel.cardView = this
            setUploadCounter(cardModel)
            setDownloadCounterAndLastSyncTime(cardModel)
            updateState(cardModel)
        }
    }

    fun updateState(cardModel: DashboardSyncCard) {
        when (cardModel.syncState) {
            SyncManagerState.NOT_STARTED -> setUIForSyncNotStarted(cardModel)
            SyncManagerState.STARTED -> setUIForSyncStarted()
            SyncManagerState.IN_PROGRESS -> setUIForSyncInProgress(cardModel)
            SyncManagerState.SUCCEED -> setUIForSyncSucceeded(cardModel)
            SyncManagerState.FAILED -> setUIForSyncFailed(cardModel)
        }
    }

    private fun setUIForSyncNotStarted(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.INVISIBLE

        if (dataModel.syncNeeded) {
            showSyncNeededText()
        }

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncStarted() {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_syncing)
        }

        syncDescription.textResource = R.string.syncing_calculating

        disableSyncButton()
    }

    private fun setUIForSyncSucceeded(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_sync_success)
        }

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncFailed(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_sync_failed)
        }

        syncDescription.textResource = R.string.dashboard_card_sync_failed

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncInProgress(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_syncing)
        }

        description.text = ""

        syncDescription.textResource = R.string.syncing

        disableSyncButton()
    }

    private fun setUploadCounter(cardModel: DashboardSyncCard) {
        syncUploadCount.text = "${Math.max(cardModel.peopleToUpload, 0)}"
    }

    private fun setDownloadCounterAndLastSyncTime(cardModel: DashboardSyncCard) {

        val observer = Observer<SyncStatus> {
            cardModel.peopleToDownload = it.peopleToDownSync
            syncDownloadCount.text = "${Math.max(it.peopleToDownSync, 0)}"
            if (it.peopleToDownSync > 0) {
                cardModel.syncNeeded = true
            }
            calculateLastSyncTimeAndUpdateText(it)
        }
        val syncStatusViewModel = SyncStatusViewModel(syncStatusDatabase)
        syncStatusViewModel.syncStatus.observe(rootView.context as DashboardActivity, observer)
    }

    private fun calculateLastSyncTimeAndUpdateText(syncStatus: SyncStatus) {
        val lastSyncTime = calculateLatestSyncTime(syncStatus.lastDownSyncTime, syncStatus.lastUpSyncTime)
        syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_sync_last_sync),
            lastSyncTime)
    }

    private fun calculateLatestSyncTime(lastDownSyncTime: String?, lastUpSyncTime: String?): String {
        val lastDownSyncDate = lastDownSyncTime?.let { dateFormat.parse(it) }
        val lastUpSyncDate =  lastUpSyncTime?.let { dateFormat.parse(it) }

        if (lastDownSyncDate != null && lastUpSyncDate != null) {
            return if (lastDownSyncDate.after(lastUpSyncDate)) {
                lastDownSyncDate.toString()
            } else {
                lastUpSyncDate.toString()
            }
        }

        lastDownSyncDate?.let { return it.toString() }
        lastUpSyncDate?.let { return it.toString() }

        return ""
    }


    private fun showSyncNeededText() {
        syncDescription.textResource = R.string.dashboard_card_sync_needed
    }

    private fun enableSyncButton(dataModel: DashboardSyncCard) {
        syncAction.apply {
            textColor = ContextCompat.getColor(rootView.context, R.color.colorAccent)
        }
        syncCard.apply {
            setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
        }
    }

    private fun disableSyncButton() {
        syncAction.apply {
            textColor = ContextCompat.getColor(rootView.context, R.color.simprints_grey)
        }
        syncCard.apply {
            setOnClickListener { }
        }
    }
}
