package com.simprints.id.activities.dashboard.cards.sync

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import java.util.*
import android.widget.LinearLayout
import org.jetbrains.anko.layoutInflater


class DashboardSyncCardDisplayerImpl(val androidResourcesHelper: AndroidResourcesHelper,
                                     val ctx: Context) : DashboardSyncCardDisplayer {

    private lateinit var root: LinearLayout
    private lateinit var viewForDefaultState: View
    private lateinit var viewForSyncFailedState: View
    private lateinit var viewForTryAgainState: View
    private lateinit var viewForNoModulesState: View
    private lateinit var viewForOfflineState: View
    private lateinit var viewForProgressState: View
    private lateinit var viewForConnectingState: View
    private lateinit var viewForCompleteState: View

    override fun initRoot(rootLayout: LinearLayout) {
        root = rootLayout
        viewForDefaultState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_default, root, false)
        viewForSyncFailedState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_failed, root, false)
        viewForTryAgainState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_try_again, root, false)
        viewForNoModulesState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_modules, root, false)
        viewForOfflineState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_settings, root, false)
        viewForProgressState = ctx.layoutInflater.inflate(R.layout.activity_dashboard_card_sync_progress, root, false)
        viewForConnectingState = viewForProgressState
        viewForCompleteState = viewForProgressState

    }

    override fun displayState(syncCardState: DashboardSyncCardState) {
        removeOldViewState()
        when (syncCardState) {
            is SyncDefault -> prepareSyncDefaultStateView(syncCardState)
            is SyncFailed -> prepareSyncFailedStateView(syncCardState)
            is SyncTryAgain -> prepareTryAgainStateView(syncCardState)
            is SyncNoModules -> prepareNoModulesStateView(syncCardState)
            is SyncOffline -> prepareSyncOfflineView(syncCardState)
            is SyncProgress -> prepareProgressView(syncCardState)
            is SyncConnecting -> prepareSyncConnectingView(syncCardState)
            is SyncComplete -> prepareSyncCompleteView(syncCardState)
        }.also {
            root.addView(it)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun prepareSyncCompleteView(syncCardState: SyncComplete): View =
        viewForCompleteState.apply {
            progressCardConnectingProgress().visibility = GONE
            progressCardSyncProgress().visibility = GONE
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().setTextColor(R.color.simprints_green)
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_complete)
            displayLastSyncTime(syncCardState, lastSyncText())

            visibility = VISIBLE
        }


    private fun prepareSyncConnectingView(syncCardState: SyncConnecting): View =
        viewForConnectingState.apply {
            progressCardConnectingProgress().visibility = VISIBLE
            progressCardConnectingProgress().isIndeterminate = true
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().isIndeterminate = true
            progressCardSyncProgress().progress = syncCardState.progress / syncCardState.total
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_connecting)
            displayLastSyncTime(syncCardState, lastSyncText())

            visibility = VISIBLE
        }

    private fun prepareProgressView(syncCardState: SyncProgress): View =
        viewForProgressState.apply {
            progressCardConnectingProgress().visibility = GONE
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().progress = syncCardState.progress / syncCardState.total
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_progress, arrayOf(syncCardState.progress / syncCardState.total))
            displayLastSyncTime(syncCardState, lastSyncText())

            visibility = VISIBLE
        }

    private fun prepareSyncOfflineView(syncCardState: DashboardSyncCardState): View =
        viewForOfflineState.apply {
            displayLastSyncTime(syncCardState, lastSyncText())
            visibility = VISIBLE
        }

    private fun prepareNoModulesStateView(syncCardState: DashboardSyncCardState): View =
        viewForNoModulesState.apply {
            displayLastSyncTime(syncCardState, lastSyncText())
            visibility = VISIBLE
        }

    private fun prepareTryAgainStateView(syncCardState: DashboardSyncCardState): View =
        viewForTryAgainState.apply {
            displayLastSyncTime(syncCardState, lastSyncText())
            visibility = VISIBLE
        }

    private fun prepareSyncDefaultStateView(syncCardState: DashboardSyncCardState): View =
        viewForDefaultState.apply {
            displayLastSyncTime(syncCardState, lastSyncText())
            visibility = VISIBLE
        }

    private fun prepareSyncFailedStateView(syncCardState: DashboardSyncCardState): View =
        viewForSyncFailedState.apply {
            displayLastSyncTime(syncCardState, lastSyncText())
            visibility = VISIBLE
        }

    private fun displayLastSyncTime(syncCardState: DashboardSyncCardState, textView: TextView) {
        textView.text = DateUtils.getRelativeTimeSpanString(syncCardState.lastSyncTime.time, Calendar.getInstance().timeInMillis, DateUtils.MINUTE_IN_MILLIS)
    }

    private fun removeOldViewState() {
        root.removeAllViews()
    }

    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_indeterminate_progress)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_sync_progress)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)
    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)

}
