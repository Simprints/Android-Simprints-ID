package com.simprints.id.activities.dashboard.cards.sync

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import java.util.*

class DashboardSyncCardDisplayerImpl(val androidResourcesHelper: AndroidResourcesHelper) : DashboardSyncCardDisplayer {

    private var defaultColor: Int = 0

    private lateinit var viewForDefaultState: View
    private lateinit var viewForSyncFailedState: View
    private lateinit var viewForTryAgainState: View
    private lateinit var viewForNoModulesState: View
    private lateinit var viewForOfflineState: View
    private lateinit var viewForProgressState: View
    private lateinit var viewForConnectingState: View
    private lateinit var viewForCompleteState: View

    override fun initViews(cardViews: ViewGroup) {
        with(cardViews) {
            defaultColor = progressCardStateText().textColors.defaultColor

            viewForDefaultState = findViewById(R.id.activity_dashboard_card_sync_default)
            viewForSyncFailedState = findViewById(R.id.activity_dashboard_card_sync_failed)
            viewForTryAgainState = findViewById(R.id.activity_dashboard_card_sync_try_again)
            viewForNoModulesState = findViewById(R.id.dashboard_sync_card_select_modules)
            viewForOfflineState = findViewById(R.id.activity_dashboard_card_sync_settings)
            viewForProgressState = findViewById(R.id.activity_dashboard_card_sync_progress)
            viewForConnectingState = findViewById(R.id.activity_dashboard_card_sync_progress)
            viewForCompleteState = findViewById(R.id.activity_dashboard_card_sync_progress)
        }
    }

    override fun displayState(syncCardState: DashboardSyncCardState) {
        hideCardViews()
        when (syncCardState) {
            is SyncDefault -> displaySyncDefaultState(syncCardState)
            is SyncFailed -> displaySyncFailedState(syncCardState)
            is SyncTryAgain -> displayTryAgainState(syncCardState)
            is SyncNoModules -> displayNoModulesState(syncCardState)
            is SyncOffline -> displaySyncOffline(syncCardState)
            is SyncProgress -> displayProgress(syncCardState)
            is SyncConnecting -> displaySyncConnecting(syncCardState)
            is SyncComplete -> displaySyncComplete(syncCardState)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun displaySyncComplete(syncCardState: SyncComplete) {
        with(viewForProgressState) {
            progressCardConnectingProgress().visibility = GONE
            progressCardSyncProgress().visibility = GONE
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().setTextColor(R.color.simprints_green)
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_complete)
            displayLastSyncTime(syncCardState, this.lastSyncText())

            viewForCompleteState.visibility = VISIBLE
        }
    }

    private fun displaySyncConnecting(syncCardState: SyncConnecting) {
        with(viewForProgressState) {
            progressCardConnectingProgress().visibility = VISIBLE
            progressCardConnectingProgress().isIndeterminate = true
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().isIndeterminate = true
            progressCardSyncProgress().progress = syncCardState.progress / syncCardState.total
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().setTextColor(defaultColor)
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_connecting)
            displayLastSyncTime(syncCardState, this.lastSyncText())

            viewForConnectingState.visibility = VISIBLE
        }
    }

    private fun displayProgress(syncCardState: SyncProgress) {
        with(viewForProgressState) {
            progressCardConnectingProgress().visibility = GONE
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().progress = syncCardState.progress / syncCardState.total
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().setTextColor(defaultColor)
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_progress, arrayOf(syncCardState.progress / syncCardState.total))
            displayLastSyncTime(syncCardState, this.lastSyncText())

            viewForProgressState.visibility = VISIBLE
        }
    }

    private fun displaySyncOffline(syncCardState: DashboardSyncCardState) {
        displayLastSyncTime(syncCardState, viewForOfflineState.lastSyncText())
        viewForOfflineState.visibility = VISIBLE
    }

    private fun displayNoModulesState(syncCardState: DashboardSyncCardState) {
        displayLastSyncTime(syncCardState, viewForNoModulesState.lastSyncText())
        viewForNoModulesState.visibility = VISIBLE
    }

    private fun displayTryAgainState(syncCardState: DashboardSyncCardState) {
        displayLastSyncTime(syncCardState, viewForTryAgainState.lastSyncText())
        viewForTryAgainState.visibility = VISIBLE
    }

    private fun displaySyncDefaultState(syncCardState: DashboardSyncCardState) {
        displayLastSyncTime(syncCardState, viewForDefaultState.lastSyncText())
        viewForDefaultState.visibility = VISIBLE
    }

    private fun displaySyncFailedState(syncCardState: DashboardSyncCardState) {
        displayLastSyncTime(syncCardState, viewForSyncFailedState.lastSyncText())
        viewForSyncFailedState.visibility = VISIBLE
    }

    private fun displayLastSyncTime(syncCardState: DashboardSyncCardState, textView: TextView) {
        textView.text = DateUtils.getRelativeTimeSpanString(syncCardState.lastSyncTime.time, Calendar.getInstance().timeInMillis, DateUtils.MINUTE_IN_MILLIS)
    }

    private fun hideCardViews() {
        viewForDefaultState.visibility = GONE
        viewForSyncFailedState.visibility = GONE
        viewForTryAgainState.visibility = GONE
        viewForNoModulesState.visibility = GONE
        viewForOfflineState.visibility = GONE
        viewForProgressState.visibility = GONE
        viewForConnectingState.visibility = GONE
        viewForCompleteState.visibility = GONE
    }

    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_indeterminate_progress)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_sync_progress)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)
    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)

}
