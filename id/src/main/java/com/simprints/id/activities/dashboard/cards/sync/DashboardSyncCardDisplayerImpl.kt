package com.simprints.id.activities.dashboard.cards.sync

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.getColorResCompat
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColor
import java.util.*


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
        ctx.layoutInflater.inflate(layout, root, false).also {
            it.textViewCardTitle().text = androidResourcesHelper.getString(R.string.dashboard_card_sync_title)
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

    private fun prepareSyncCompleteView(syncCardState: SyncComplete): View =
        withVisible(viewForCompleteState) {
            progressCardConnectingProgress().visibility = GONE
            withVisible(progressCardSyncProgress()) {
                progress = 100
            }
            withVisible(progressCardStateText()) {
                setTextColor(androidResourcesHelper.getColorStateList(R.color.simprints_green))
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_complete)
            }
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }


    private fun prepareSyncConnectingView(syncCardState: SyncConnecting): View =
        withVisible(viewForConnectingState) {
            progressCardConnectingProgress().visibility = VISIBLE
            setPercentageInProgressBar(progressCardSyncProgress(), syncCardState.progress, syncCardState.total)
            withVisible(progressCardStateText()) {
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_connecting)
                textColor = context.getColorResCompat(android.R.attr.textColorPrimary)
            }

            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareProgressView(syncCardState: SyncProgress): View =
        withVisible(viewForProgressState) {
            progressCardConnectingProgress().visibility = GONE
            setPercentageInProgressBar(progressCardSyncProgress(), syncCardState.progress, syncCardState.total)

            withVisible(progressCardStateText()) {
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_progress, arrayOf("${syncCardState.progress}/${syncCardState.total}"))
                textColor = context.getColorResCompat(android.R.attr.textColorPrimary)
            }
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareSyncOfflineView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForOfflineState) {
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareNoModulesStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForNoModulesState) {
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareTryAgainStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForTryAgainState) {
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareSyncDefaultStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForDefaultState) {
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun prepareSyncFailedStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForSyncFailedState) {
            displayLastSyncTime(syncCardState.lastSyncTime, lastSyncText())
        }

    private fun displayLastSyncTime(lastSyncTime: Date, textView: TextView) {
        val lastSyncTimeText = timeHelper.readableBetweenNowAndTime(lastSyncTime)
        textView.text = androidResourcesHelper.getString(R.string.dashboard_card_sync_last_sync, arrayOf(lastSyncTimeText))
    }

    private fun removeOldViewState() {
        root.removeAllViews()
    }

    private inline fun <T : View> withVisible(view: T, block: T.() -> Unit): T =
        view.apply {
            block(view)
            visibility = VISIBLE
        }

    private fun setPercentageInProgressBar(progressBar: ProgressBar,
                                           progressValue: Int,
                                           totalValue: Int) {
        withVisible(progressBar) {
            progress = getPercentageForProgress(progressValue, totalValue)
        }
    }

    private fun getPercentageForProgress(progress: Int, total: Int) =
        (100 * (progress.toFloat() / total.toFloat())).toInt()


    private fun View.textViewCardTitle() = this.findViewById<TextView>(R.id.dashboard_sync_card_title)
    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_sync_progress_bar)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)
    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)
}
