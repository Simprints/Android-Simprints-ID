package com.simprints.id.activities.dashboard.cards.sync

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.getColorResCompat
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColor


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

    override val userWantsToSelectAModule = MutableLiveData<LiveDataEvent>()
    override val userWantsToOpenSettings = MutableLiveData<LiveDataEvent>()
    override val userWantsToSync = MutableLiveData<LiveDataEvent>()

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

    private fun prepareSyncCompleteView(syncCardState: SyncComplete): View =
        withVisible(viewForCompleteState) {
            progressCardConnectingProgress().visibility = GONE
            withVisible(progressCardSyncProgress()) {
                progress = 100
                val green = androidResourcesHelper.getColorStateList(R.color.simprints_green)
                green?.let {
                    progressDrawable.setColorFilter(it.defaultColor, PorterDuff.Mode.SRC_IN)
                }
            }
            withVisible(progressCardStateText()) {
                setTextColor(androidResourcesHelper.getColorStateList(R.color.simprints_green))
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_complete)
            }
            displayLastSyncTime(syncCardState, lastSyncText())
        }


    private fun prepareSyncConnectingView(syncCardState: SyncConnecting): View =
        withVisible(viewForConnectingState) {
            withVisible(progressCardStateText()) {
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_connecting)
                textColor = context.getColorResCompat(android.R.attr.textColorPrimary)
            }
            withVisible(progressCardSyncProgress()) {
                setSyncProgress(syncCardState.progress, syncCardState.total)
            }

            displayLastSyncTime(syncCardState, lastSyncText())
        }


    private fun prepareProgressView(syncCardState: SyncProgress): View =
        withVisible(viewForProgressState) {
            progressCardConnectingProgress().visibility = GONE
            withVisible(progressCardStateText()) {
                val progress = syncCardState.progress.toString()
                val total = syncCardState.total ?: "?"
                text = androidResourcesHelper.getString(R.string.dashboard_sync_card_progress, arrayOf("$progress/$total"))
                textColor = context.getColorResCompat(android.R.attr.textColorPrimary)
            }
            withVisible(progressCardSyncProgress()) {
                setSyncProgress(syncCardState.progress, syncCardState.total)
            }
            displayLastSyncTime(syncCardState, lastSyncText())
        }

    private fun prepareSyncOfflineView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForOfflineState) {
            displayLastSyncTime(syncCardState, lastSyncText())
            buttonOpenSettings().setOnClickListener {
                userWantsToOpenSettings.send()
            }
        }

    private fun prepareNoModulesStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForNoModulesState) {
            displayLastSyncTime(syncCardState, lastSyncText())
            buttonSelectModule().setOnClickListener {
                userWantsToSelectAModule.send()
            }

        }

    private fun prepareTryAgainStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForTryAgainState) {
            displayLastSyncTime(syncCardState, lastSyncText())
            buttonProgressSync().setOnClickListener {
                userWantsToSync.send()
            }
        }

    private fun prepareSyncDefaultStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForDefaultState) {
            displayLastSyncTime(syncCardState, lastSyncText())
            buttonDefaultSync().setOnClickListener {
                userWantsToSync.send()
            }
        }

    private fun prepareSyncFailedStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForSyncFailedState) {
            displayLastSyncTime(syncCardState, lastSyncText())
        }

    private fun displayLastSyncTime(syncCardState: DashboardSyncCardState, textView: TextView) {
        val lastSyncTimeText = syncCardState.lastSyncTime?.let { timeHelper.readableBetweenNowAndTime(it) } ?: ""
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

    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_sync_progress_bar)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)

    private fun View.buttonSelectModule() = this.findViewById<TextView>(R.id.dashboard_sync_card_select_no_modules_button)
    private fun View.buttonOpenSettings() = this.findViewById<Button>(R.id.dashboard_sync_card_offline_button)
    private fun View.buttonDefaultSync() = this.findViewById<Button>(R.id.dashboard_sync_card_default_state_sync_button)
    private fun View.buttonProgressSync() = this.findViewById<Button>(R.id.dashboard_sync_card_try_again_sync_button)

    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)
    private fun ProgressBar.setSyncProgress(progressValue: Int, totalValue: Int?) =
        if (totalValue != null) {
            isIndeterminate = false
            progress = (100 * (progressValue.toFloat() / totalValue.toFloat())).toInt()
        } else {
            isIndeterminate = true
        }

}
