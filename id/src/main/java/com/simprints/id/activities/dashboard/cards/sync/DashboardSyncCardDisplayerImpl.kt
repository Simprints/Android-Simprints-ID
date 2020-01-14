package com.simprints.id.activities.dashboard.cards.sync

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import android.widget.LinearLayout
import com.simprints.id.tools.TimeHelper
import org.jetbrains.anko.layoutInflater


class DashboardSyncCardDisplayerImpl(val androidResourcesHelper: AndroidResourcesHelper,
                                     val timeHelper: TimeHelper,
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

    override fun initRoot(syncCardsRootLayout: LinearLayout) {
        root = syncCardsRootLayout
        viewForDefaultState = createViewForSyncState(R.layout.activity_dashboard_card_sync_default, root)
        viewForSyncFailedState = createViewForSyncState(R.layout.activity_dashboard_card_sync_failed, root)
        viewForTryAgainState = createViewForSyncState(R.layout.activity_dashboard_card_sync_try_again, root)
        viewForNoModulesState = createViewForSyncState(R.layout.activity_dashboard_card_sync_modules, root)
        viewForOfflineState = createViewForSyncState(R.layout.activity_dashboard_card_sync_offline, root)
        viewForProgressState = createViewForSyncState(R.layout.activity_dashboard_card_sync_progress, root)
        viewForConnectingState = viewForProgressState
        viewForCompleteState = viewForProgressState

    }

    private fun createViewForSyncState(layout: Int, root: ViewGroup) =
        ctx.layoutInflater.inflate(layout, root, false)

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
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().progress = 100
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().setTextColor(R.color.simprints_green)
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_complete)
            displayLastSyncTime(syncCardState, lastSyncText())

            visibility = VISIBLE
        }


    private fun prepareSyncConnectingView(syncCardState: SyncConnecting): View =
        viewForConnectingState.apply {
            progressCardConnectingProgress().visibility = VISIBLE
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().progress = (100 * (syncCardState.progress.toFloat() / syncCardState.total.toFloat())).toInt()
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_connecting)
            displayLastSyncTime(syncCardState, lastSyncText())

            visibility = VISIBLE
        }

    private fun prepareProgressView(syncCardState: SyncProgress): View =
        viewForProgressState.apply {
            progressCardConnectingProgress().visibility = GONE
            progressCardSyncProgress().visibility = VISIBLE
            progressCardSyncProgress().progress = (100 * (syncCardState.progress.toFloat() / syncCardState.total.toFloat())).toInt()
            progressCardStateText().visibility = VISIBLE
            progressCardStateText().text = androidResourcesHelper.getString(R.string.sync_card_progress, arrayOf("${syncCardState.progress}/${syncCardState.total}"))
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
        val lastSyncTimeText = timeHelper.readableBetweenNowAndTime(syncCardState.lastSyncTime)
        textView.text = androidResourcesHelper.getString(R.string.dashboard_card_sync_last_sync, arrayOf(lastSyncTimeText))
    }

    private fun removeOldViewState() {
        root.removeAllViews()
    }

    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_sync_progress_bar)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)
    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)

}
