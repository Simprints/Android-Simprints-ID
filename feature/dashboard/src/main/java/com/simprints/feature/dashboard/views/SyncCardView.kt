package com.simprints.feature.dashboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.material.card.MaterialCardView
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.dashboard.databinding.LayoutCardSyncBinding
import com.simprints.infra.resources.R
import kotlin.math.min

internal class SyncCardView : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    var onSyncButtonClick: () -> Unit = {}
    var onSelectNoModulesButtonClick: () -> Unit = {}
    var onOfflineButtonClick: () -> Unit = {}
    var onLoginButtonClick: () -> Unit = {}
    private val binding = LayoutCardSyncBinding.inflate(LayoutInflater.from(context), this)

    init {
        hideAllViews()
    }

    internal fun render(state: SyncCardState) {
        hideAllViews()
        when (state) {
            is SyncCardState.SyncDefault -> prepareSyncDefaultStateView()
            is SyncCardState.SyncPendingUpload -> prepareSyncDefaultStateView(state.itemsToUpSync)
            is SyncCardState.SyncFailed -> prepareSyncFailedStateView()
            is SyncCardState.SyncFailedReloginRequired -> prepareSyncFailedBecauseReloginRequired()
            is SyncCardState.SyncFailedBackendMaintenance -> prepareSyncFailedBecauseBackendMaintenanceView(state)
            is SyncCardState.SyncTooManyRequests -> prepareSyncTooManyRequestsView()
            is SyncCardState.SyncTryAgain -> prepareTryAgainStateView()
            is SyncCardState.SyncHasNoModules -> prepareNoModulesStateView()
            is SyncCardState.SyncOffline -> prepareSyncOfflineView()
            is SyncCardState.SyncProgress -> prepareProgressView(state)
            is SyncCardState.SyncConnecting -> prepareSyncConnectingView(state)
            is SyncCardState.SyncComplete -> prepareSyncCompleteView()
        }
        updateLastSyncTime(state.lastTimeSyncSucceed)
    }

    private fun hideAllViews() {
        binding.syncCardDefault.visibility = View.GONE
        binding.syncCardFailedMessage.visibility = View.GONE
        binding.syncCardSelectNoModules.visibility = View.GONE
        binding.syncCardOffline.visibility = View.GONE
        binding.syncCardProgress.visibility = View.GONE
        binding.syncCardTryAgain.visibility = View.GONE
        binding.syncCardReloginRequired.visibility = View.GONE
    }

    private fun prepareSyncDefaultStateView(itemsToSync: Int = 0) {
        binding.syncCardDefault.visibility = View.VISIBLE
        binding.syncCardDefaultStateSyncButton.setOnClickListener { onSyncButtonClick() }
        binding.syncCardDefaultItemsToUpload.text = if (itemsToSync <= 0) {
            resources.getString(R.string.dashboard_sync_card_records_uploaded)
        } else {
            resources.getQuantityString(
                R.plurals.dashboard_sync_card_records_to_upload,
                itemsToSync,
                itemsToSync,
            )
        }
    }

    private fun prepareSyncFailedStateView() {
        binding.syncCardFailedMessage.visibility = View.VISIBLE
        binding.syncCardFailedMessage.text =
            resources.getString(R.string.dashboard_sync_card_failed_message)
    }

    private fun prepareSyncFailedBecauseReloginRequired() {
        binding.syncCardReloginRequired.visibility = View.VISIBLE
        binding.syncCardReloginRequiredLoginButton.setOnClickListener { onLoginButtonClick() }
    }

    private fun prepareSyncFailedBecauseBackendMaintenanceView(state: SyncCardState.SyncFailedBackendMaintenance) {
        binding.syncCardFailedMessage.visibility = View.VISIBLE
        binding.syncCardFailedMessage.text =
            if (state.estimatedOutage != null && state.estimatedOutage != 0L) {
                resources.getString(
                    R.string.error_backend_maintenance_with_time_message,
                    TimeUtils.getFormattedEstimatedOutage(state.estimatedOutage),
                )
            } else {
                resources.getString(R.string.error_backend_maintenance_message)
            }
    }

    private fun prepareSyncTooManyRequestsView() {
        binding.syncCardFailedMessage.visibility = View.VISIBLE
        binding.syncCardFailedMessage.text =
            resources.getString(R.string.dashboard_sync_card_too_many_modules_message)
    }

    private fun prepareTryAgainStateView() {
        binding.syncCardTryAgain.visibility = View.VISIBLE
        binding.syncCardTryAgainSyncButton.setOnClickListener { onSyncButtonClick() }
    }

    private fun prepareNoModulesStateView() {
        binding.syncCardSelectNoModules.visibility = View.VISIBLE
        binding.syncCardSelectNoModulesButton.setOnClickListener {
            onSelectNoModulesButtonClick()
        }
    }

    private fun prepareSyncOfflineView() {
        binding.syncCardOffline.visibility = View.VISIBLE
        binding.syncCardOfflineButton.setOnClickListener { onOfflineButtonClick() }
    }

    private fun prepareProgressView(state: SyncCardState.SyncProgress) {
        binding.syncCardProgress.visibility = View.VISIBLE

        val percentage = if (state.progress != null && state.total != null) {
            "${calculatePercentage(state.progress, state.total)}%"
        } else {
            ""
        }
        binding.syncCardProgressMessage.text = resources.getString(
            R.string.dashboard_sync_card_progress,
            percentage,
        )
        binding.syncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, R.color.simprints_blue_dark)
    }

    private fun prepareSyncConnectingView(state: SyncCardState.SyncConnecting) {
        binding.syncCardProgress.visibility = View.VISIBLE

        binding.syncCardProgressMessage.text =
            resources.getString(R.string.dashboard_sync_card_connecting)
        binding.syncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, R.color.simprints_blue_dark)
    }

    private fun prepareSyncCompleteView() {
        binding.syncCardProgress.visibility = View.VISIBLE

        binding.syncCardProgressMessage.text =
            resources.getString(R.string.dashboard_sync_card_complete)
        binding.syncCardProgressMessage.setTextColor(context?.getColorStateList(R.color.simprints_green_dark))

        setProgress(100, 100, R.color.simprints_green_dark)
    }

    private fun updateLastSyncTime(lastSync: String?) {
        if (lastSync == null) {
            binding.syncCardLastSync.visibility = View.GONE
        } else {
            binding.syncCardLastSync.visibility = View.VISIBLE
            binding.syncCardLastSync.text = String.format(
                resources.getString(R.string.dashboard_sync_card_last_sync),
                lastSync,
            )
        }
    }

    private fun setProgress(
        progress: Int?,
        total: Int?,
        color: Int,
    ) {
        with(binding.syncCardProgressSyncProgressBar) {
            if (progress != null && total != null) {
                setProgressBarIndeterminate(this, false)
                this.progress = calculatePercentage(progress, total)
            } else {
                setProgressBarIndeterminate(this, true)
            }
            setProgressColor(color, this)
        }
    }

    private fun setProgressColor(
        color: Int,
        progressBar: ProgressBar,
    ) {
        context?.getColorStateList(color)?.defaultColor?.let {
            progressBar.progressDrawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color,
                    BlendModeCompat.SRC_IN,
                )
            progressBar.indeterminateDrawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color,
                    BlendModeCompat.SRC_IN,
                )
        }
    }

    private fun setProgressBarIndeterminate(
        progressBar: ProgressBar,
        value: Boolean,
    ) {
        // Setting it only when required otherwise it creates glitches
        if (progressBar.isIndeterminate != value) {
            progressBar.isIndeterminate = value
        }
    }

    private fun calculatePercentage(
        progressValue: Int,
        totalValue: Int,
    ) = min((100 * (progressValue.toFloat() / totalValue.toFloat())).toInt(), 100)

    // I couldn't find a way to get from Android SDK the default text color (in line with the theme).
    // So I change a color for a TextView, then I can't set back to the default.
    // The card's title has always the same color - the default one.
    // Hacky way to extract the color from the title and use for the other TextViews
    private fun getDefaultGrayTextColor(): Int = binding.syncCardTitle.textColors.defaultColor
}
