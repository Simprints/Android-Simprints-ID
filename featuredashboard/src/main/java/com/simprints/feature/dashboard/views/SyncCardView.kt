package com.simprints.feature.dashboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.dashboard.databinding.LayoutCardSyncBinding
import com.simprints.feature.dashboard.main.sync.DashboardSyncCardState
import com.simprints.infra.resources.R
import kotlin.math.min

internal class SyncCardView : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var onSyncButtonClick: () -> Unit = {}
    var onSelectNoModulesButtonClick: () -> Unit = {}
    var onOfflineButtonClick: () -> Unit = {}
    private val binding = LayoutCardSyncBinding.inflate(LayoutInflater.from(context), this)

    init {
        hideAllViews()
    }

    internal fun render(state: DashboardSyncCardState) {
        hideAllViews()
        when (state) {
            is DashboardSyncCardState.SyncDefault -> prepareSyncDefaultStateView()
            is DashboardSyncCardState.SyncPendingUpload -> prepareSyncDefaultStateView(state.itemsToUpSync)
            is DashboardSyncCardState.SyncFailed -> prepareSyncFailedStateView()
            is DashboardSyncCardState.SyncFailedBackendMaintenance -> prepareSyncFailedBecauseBackendMaintenanceView(
                state
            )

            is DashboardSyncCardState.SyncTooManyRequests -> prepareSyncTooManyRequestsView()
            is DashboardSyncCardState.SyncTryAgain -> prepareTryAgainStateView()
            is DashboardSyncCardState.SyncHasNoModules -> prepareNoModulesStateView()
            is DashboardSyncCardState.SyncOffline -> prepareSyncOfflineView()
            is DashboardSyncCardState.SyncProgress -> prepareProgressView(state)
            is DashboardSyncCardState.SyncConnecting -> prepareSyncConnectingView(state)
            is DashboardSyncCardState.SyncComplete -> prepareSyncCompleteView()
        }
        updateLastSyncTime(state.lastTimeSyncSucceed)
    }

    private fun hideAllViews() {
        binding.dashboardSyncCardDefault.visibility = View.GONE
        binding.dashboardSyncCardFailedMessage.visibility = View.GONE
        binding.dashboardSyncCardSelectNoModules.visibility = View.GONE
        binding.dashboardSyncCardOffline.visibility = View.GONE
        binding.dashboardSyncCardProgress.visibility = View.GONE
        binding.dashboardSyncCardTryAgain.visibility = View.GONE
    }

    private fun prepareSyncDefaultStateView(itemsToSync: Int = 0) {
        binding.dashboardSyncCardDefault.visibility = View.VISIBLE
        binding.dashboardSyncCardDefaultStateSyncButton.setOnClickListener { onSyncButtonClick() }
        binding.dashboardSyncCardDefaultItemsToUpload.text = if (itemsToSync <= 0) {
            resources.getString(R.string.dashboard_sync_card_records_uploaded)
        } else {
            resources.getQuantityString(
                R.plurals.dashboard_sync_card_records_to_upload,
                itemsToSync,
                itemsToSync
            )
        }
    }

    private fun prepareSyncFailedStateView() {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            resources.getString(R.string.dashboard_sync_card_failed_message)
    }

    private fun prepareSyncFailedBecauseBackendMaintenanceView(state: DashboardSyncCardState.SyncFailedBackendMaintenance) {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            if (state.estimatedOutage != null && state.estimatedOutage != 0L)
                resources.getString(
                    R.string.error_backend_maintenance_with_time_message,
                    TimeUtils.getFormattedEstimatedOutage(state.estimatedOutage)
                )
            else
                resources.getString(R.string.error_backend_maintenance_message)
    }

    private fun prepareSyncTooManyRequestsView() {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            resources.getString(R.string.dashboard_sync_card_too_many_modules_message)
    }

    private fun prepareTryAgainStateView() {
        binding.dashboardSyncCardTryAgain.visibility = View.VISIBLE
        binding.dashboardSyncCardTryAgainSyncButton.setOnClickListener {
            onSyncButtonClick()
        }
    }

    private fun prepareNoModulesStateView() {
        binding.dashboardSyncCardSelectNoModules.visibility = View.VISIBLE
        binding.dashboardSyncCardSelectNoModulesButton.setOnClickListener {
            onSelectNoModulesButtonClick()
        }
    }

    private fun prepareSyncOfflineView() {
        binding.dashboardSyncCardOffline.visibility = View.VISIBLE
        binding.dashboardSyncCardOfflineButton.setOnClickListener {
            onOfflineButtonClick()
        }
    }

    private fun prepareProgressView(state: DashboardSyncCardState.SyncProgress) {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.GONE

        val percentage = if (state.total != null)
            "${calculatePercentage(state.progress, state.total)}%"
        else
            ""
        binding.dashboardSyncCardProgressMessage.text = resources.getString(
            R.string.dashboard_sync_card_progress,
            percentage
        )
        binding.dashboardSyncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, R.color.colorPrimaryDark)
    }

    private fun prepareSyncConnectingView(state: DashboardSyncCardState.SyncConnecting) {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.VISIBLE

        binding.dashboardSyncCardProgressMessage.text =
            resources.getString(R.string.dashboard_sync_card_connecting)
        binding.dashboardSyncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, R.color.colorPrimaryDark)
    }

    private fun prepareSyncCompleteView() {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.GONE

        binding.dashboardSyncCardProgressMessage.text =
            resources.getString(R.string.dashboard_sync_card_complete)
        binding.dashboardSyncCardProgressMessage.setTextColor(context?.getColorStateList(R.color.simprints_green_dark))

        setProgress(100, 100, R.color.simprints_green_dark)
    }

    private fun updateLastSyncTime(lastSync: String?) {
        if (lastSync == null) {
            binding.dashboardSyncCardLastSync.visibility = View.GONE
        } else {
            binding.dashboardSyncCardLastSync.visibility = View.VISIBLE
            binding.dashboardSyncCardLastSync.text = String.format(
                resources.getString(R.string.dashboard_card_sync_last_sync),
                lastSync
            )
        }
    }

    private fun setProgress(progress: Int, total: Int?, color: Int) {
        with(binding.dashboardSyncCardProgressSyncProgressBar) {
            if (total != null) {
                setProgressBarIndeterminate(this, false)
                this.progress = calculatePercentage(progress, total)
            } else {
                setProgressBarIndeterminate(this, true)
            }
            setProgressColor(color, this)
        }
    }

    private fun setProgressColor(color: Int, progressBar: ProgressBar) {
        context?.getColorStateList(color)?.defaultColor?.let {
            progressBar.progressDrawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color,
                    BlendModeCompat.SRC_IN
                )
            progressBar.indeterminateDrawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color,
                    BlendModeCompat.SRC_IN
                )
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


    // I couldn't find a way to get from Android SDK the default text color (in line with the theme).
    // So I change a color for a TextView, then I can't set back to the default.
    // The card's title has always the same color - the default one.
    // Hacky way to extract the color from the title and use for the other TextViews
    private fun getDefaultGrayTextColor(): Int =
        binding.dashboardSyncCardTitle.textColors.defaultColor

}
