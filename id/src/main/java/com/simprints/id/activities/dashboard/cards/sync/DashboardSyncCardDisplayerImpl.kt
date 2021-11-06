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
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColor
import java.util.*
import kotlin.math.min


class DashboardSyncCardDisplayerImpl(val timeHelper: TimeHelper) : DashboardSyncCardDisplayer {

    var tickerToUpdateLastSyncTimeText: ReceiveChannel<Unit>? = null
    private var lastSyncTimeTextView: TextView? = null
    private var cachedLastSyncTime: Date? = null

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
        root.context.layoutInflater.inflate(layout, root, false).also {
            it.visibility = GONE
            it.textViewCardTitle().text = it.context.getString(R.string.dashboard_card_sync_title)
        }

    override fun displayState(syncCardState: DashboardSyncCardState) {
        removeOldViewState()

        when (syncCardState) {
            is SyncDefault -> prepareSyncDefaultStateView(syncCardState)
            is SyncFailed -> prepareSyncFailedStateView(syncCardState)
            is SyncTryAgain -> prepareTryAgainStateView(syncCardState)
            is SyncHasNoModules -> prepareNoModulesStateView(syncCardState)
            is SyncOffline -> prepareSyncOfflineView(syncCardState)
            is SyncProgress -> prepareProgressView(syncCardState)
            is SyncConnecting -> prepareSyncConnectingView(syncCardState)
            is SyncComplete -> prepareSyncCompleteView(syncCardState)
        }.also {
            root.addView(it)
        }
    }

    override suspend fun startTickerToUpdateLastSyncText() {
        stopOngoingTickerToUpdateLastSyncText()
        tickerToUpdateLastSyncTimeText = ticker(delayMillis = ONE_MINUTE, initialDelayMillis = 0)

        tickerToUpdateLastSyncTimeText?.let { channel ->
            for (event in channel) {
                lastSyncTimeTextView?.let {
                    updateLastSyncUI(cachedLastSyncTime, it, it.context)
                }
            }
        }
    }

    override fun stopOngoingTickerToUpdateLastSyncText() {
        tickerToUpdateLastSyncTimeText?.cancel()
    }

    private fun prepareSyncCompleteView(syncCardState: SyncComplete): View =
        withVisible(viewForCompleteState) {
            progressCardConnectingProgress().visibility = GONE
            withVisible(progressCardStateText()) {
                setTextColor(context.getColorStateList(R.color.simprints_green_dark))
                text = context.getString(R.string.dashboard_sync_card_complete)
            }
            withVisible(progressCardSyncProgress()) {
                setSyncProgress(100, 100)
                setProgressColor(getDefaultColor(R.color.simprints_green_dark, context), this)
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }


    private fun prepareSyncConnectingView(syncCardState: SyncConnecting): View =
        withVisible(viewForConnectingState) {
            withVisible(progressCardStateText()) {
                textColor = getDefaultGrayTextColor(viewForConnectingState)
                text = context.getString(R.string.dashboard_sync_card_connecting)
            }
            withVisible(progressCardSyncProgress()) {
                setSyncProgress(syncCardState.progress, syncCardState.total)
                setProgressColor(getDefaultColor(R.color.colorPrimaryDark, context), this)
            }

            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }


    private fun prepareProgressView(syncCardState: SyncProgress): View =
        withVisible(viewForProgressState) {
            progressCardConnectingProgress().visibility = GONE
            withVisible(progressCardStateText()) {
                val percentageText = if (syncCardState.total != null) {
                    "${calculatePercentage(syncCardState.progress, syncCardState.total)}%"
                } else {
                    ""
                }
                text =  String.format(context.getString(R.string.dashboard_sync_card_progress), percentageText)
                textColor = getDefaultGrayTextColor(viewForConnectingState)
            }
            withVisible(progressCardSyncProgress()) {
                setSyncProgress(syncCardState.progress, syncCardState.total)
                setProgressColor(getDefaultColor(R.color.colorPrimaryDark, context), this)
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }

    private fun prepareSyncOfflineView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForOfflineState) {
            with(titleCardOffline()) {
                text = context.getString(R.string.dashboard_sync_card_offline_message)
            }
            with(buttonOpenSettings()) {
                text = context.getString(R.string.dashboard_sync_card_offline_button)
                setOnClickListener {
                    userWantsToOpenSettings.send()
                }
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }

    private fun prepareNoModulesStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForNoModulesState) {
            with(titleCardSelectModule()) {
                text = context.getString(R.string.dashboard_sync_card_no_modules_message)
            }
            with(buttonSelectModule()) {
                text = context.getString(R.string.dashboard_sync_card_no_modules_button).capitalize()
                setOnClickListener {
                    userWantsToSelectAModule.send()
                }
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }


    private fun prepareTryAgainStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForTryAgainState) {
            with(titleCardTryAgain()) {
                text = context.getString(R.string.dashboard_sync_card_incomplete)
            }
            with(buttonProgressSync()) {
                text = context.getString(R.string.dashboard_sync_card_try_again_button).capitalize()
                setOnClickListener {
                    userWantsToSync.send()
                }
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }

    private fun prepareSyncDefaultStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForDefaultState) {
            withVisible(buttonDefaultSync()) {
                text = context.getString(R.string.dashboard_sync_card_sync_button).capitalize()
                setOnClickListener {
                    userWantsToSync.send()
                }
            }
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }

    private fun prepareSyncFailedStateView(syncCardState: DashboardSyncCardState): View =
        withVisible(viewForSyncFailedState) {
            titleCardFailed().text = context.getString(R.string.dashboard_sync_card_failed_message)
            displayLastSyncTime(syncCardState.lastTimeSyncSucceed, lastSyncText())
        }

    private fun displayLastSyncTime(lastSyncTime: Date?, textView: TextView) {
        cachedLastSyncTime = lastSyncTime
        lastSyncTimeTextView = textView
        updateLastSyncUI(cachedLastSyncTime, textView, root.context)
    }

    private fun updateLastSyncUI(lastSyncTime: Date?, textView: TextView, ctx: Context) {
        lastSyncTime?.let {
            val lastSyncTimeText = timeHelper.readableBetweenNowAndTime(it)
            textView.visibility = VISIBLE
            textView.text = String.format(ctx.getString(R.string.dashboard_card_sync_last_sync), lastSyncTimeText)
        } ?: run { textView.visibility = GONE }
    }

    private fun removeOldViewState() {
        root.removeAllViews()
    }

    private inline fun <T : View> withVisible(view: T, block: T.() -> Unit): T =
        view.apply {
            block(view)
            visibility = VISIBLE
        }

    // I couldn't find a way to get from Android SDK the default text color (in line with the theme).
    // So I change a color for a TextView, then I can't set back to the default.
    // The card's title has always the same color - the default one.
    // Hacky way to extract the color from the title and use for the other TextViews
    private fun getDefaultGrayTextColor(view: View): Int = view.textViewCardTitle().textColors.defaultColor

    private fun getDefaultColor(colorState: Int, ctx: Context) = ctx.getColorStateList(colorState)?.defaultColor

    private fun View.textViewCardTitle() = this.findViewById<TextView>(R.id.dashboard_sync_card_title)
    private fun View.progressCardConnectingProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)
    private fun View.progressCardSyncProgress() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_sync_progress_bar)
    private fun View.progressCardStateText() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message)

    private fun View.titleCardOffline() = this.findViewById<TextView>(R.id.dashboard_sync_card_offline_message)
    private fun View.titleCardTryAgain() = this.findViewById<TextView>(R.id.dashboard_sync_card_try_again_message)
    private fun View.titleCardFailed() = this.findViewById<TextView>(R.id.dashboard_sync_card_failed_message)
    private fun View.titleCardSelectModule() = this.findViewById<TextView>(R.id.dashboard_sync_card_select_no_modules_message)
    private fun View.buttonSelectModule() = this.findViewById<Button>(R.id.dashboard_sync_card_select_no_modules_button)
    private fun View.buttonOpenSettings() = this.findViewById<Button>(R.id.dashboard_sync_card_offline_button)
    private fun View.buttonDefaultSync() = this.findViewById<Button>(R.id.dashboard_sync_card_default_state_sync_button)
    private fun View.buttonProgressSync() = this.findViewById<Button>(R.id.dashboard_sync_card_try_again_sync_button)

    private fun View.lastSyncText() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)
    private fun ProgressBar.setSyncProgress(progressValue: Int, totalValue: Int?) {
        if (totalValue != null) {
            setProgressBarIndeterminate(this, false)
            progress = calculatePercentage(progressValue, totalValue)
        } else {
            // Setting it only when required otherwise it creates glitches
            setProgressBarIndeterminate(this, true)
        }
    }

    private fun setProgressColor(color: Int?, progressBar: ProgressBar) {
        color?.let {
            progressBar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setProgressBarIndeterminate(progressBar: ProgressBar, value: Boolean) {
        // Setting it only when required otherwise it creates glitches
        if (progressBar.isIndeterminate != value) {
            progressBar.isIndeterminate = value
        }
    }

    private fun calculatePercentage(progressValue: Int, totalValue: Int) =
        min((100 * (progressValue.toFloat() / totalValue.toFloat())).toInt(), 100)

    companion object {
        private const val ONE_MINUTE = 1000L * 60

    }
}
