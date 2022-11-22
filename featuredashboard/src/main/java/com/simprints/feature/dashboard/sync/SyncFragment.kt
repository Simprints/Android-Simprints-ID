package com.simprints.feature.dashboard.sync

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.utils.TimeUtils.getFormattedEstimatedOutage
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardSyncBinding
import com.simprints.feature.dashboard.sync.DashboardSyncCardState.*
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SyncFragment : Fragment(R.layout.fragment_dashboard_card_sync) {

    private val viewModel by viewModels<SyncViewModel>()
    private val binding by viewBinding(FragmentDashboardCardSyncBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideAllViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.syncToBFSIDAllowed.observe(viewLifecycleOwner) {
            if (it) {
                binding.dashboardSyncCard.visibility = View.VISIBLE
            } else {
                binding.dashboardSyncCard.visibility = View.GONE
            }
        }
        viewModel.syncCardLiveData.observe(viewLifecycleOwner) {
            displaySyncCard(it)
        }
    }

    private fun displaySyncCard(state: DashboardSyncCardState) {
        hideAllViews()
        when (state) {
            is SyncDefault -> prepareSyncDefaultStateView()
            is SyncFailed -> prepareSyncFailedStateView()
            is SyncFailedBackendMaintenance -> prepareSyncFailedBecauseBackendMaintenanceView(
                state
            )
            is SyncTooManyRequests -> prepareSyncTooManyRequestsView()
            is SyncTryAgain -> prepareTryAgainStateView()
            is SyncHasNoModules -> prepareNoModulesStateView()
            is SyncOffline -> prepareSyncOfflineView()
            is SyncProgress -> prepareProgressView(state)
            is SyncConnecting -> prepareSyncConnectingView(state)
            is SyncComplete -> prepareSyncCompleteView()
        }
        updateLastSyncTime(state.lastTimeSyncSucceed)
    }

    private fun prepareSyncDefaultStateView() {
        binding.dashboardSyncCardDefaultStateSyncButton.visibility = View.VISIBLE
    }

    private fun prepareSyncFailedStateView() {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            getString(IDR.string.dashboard_sync_card_failed_message)
    }

    private fun prepareSyncFailedBecauseBackendMaintenanceView(state: SyncFailedBackendMaintenance) {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            if (state.estimatedOutage != null && state.estimatedOutage != 0L)
                getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    getFormattedEstimatedOutage(state.estimatedOutage)
                )
            else
                getString(IDR.string.error_backend_maintenance_message)
    }

    private fun prepareSyncTooManyRequestsView() {
        binding.dashboardSyncCardFailedMessage.visibility = View.VISIBLE
        binding.dashboardSyncCardFailedMessage.text =
            getString(IDR.string.dashboard_sync_card_too_many_modules_message)
    }

    private fun prepareTryAgainStateView() {
        binding.dashboardSyncCardTryAgain.visibility = View.VISIBLE
        binding.dashboardSyncCardTryAgainSyncButton.setOnClickListener {
            viewModel.sync()
        }
    }

    private fun prepareNoModulesStateView() {
        binding.dashboardSyncCardSelectNoModules.visibility = View.VISIBLE
        binding.dashboardSyncCardSelectNoModulesButton.setOnClickListener {
            // TODO add navigation to module selection
            findNavController().navigate("")
        }
    }

    private fun prepareSyncOfflineView() {
        binding.dashboardSyncCardOffline.visibility = View.VISIBLE
        binding.dashboardSyncCardOfflineButton.setOnClickListener {
            // TODO open settings
        }
    }

    private fun prepareProgressView(state: SyncProgress) {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.GONE

        val percentage = if (state.total != null)
            "${calculatePercentage(state.progress, state.total)}%"
        else
            ""
        binding.dashboardSyncCardProgressMessage.text = getString(
            IDR.string.dashboard_sync_card_progress,
            percentage
        )
        binding.dashboardSyncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, IDR.color.colorPrimaryDark)
    }

    private fun prepareSyncConnectingView(state: SyncConnecting) {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.VISIBLE

        binding.dashboardSyncCardProgressMessage.text =
            getString(IDR.string.dashboard_sync_card_connecting)
        binding.dashboardSyncCardProgressMessage.setTextColor(getDefaultGrayTextColor())

        setProgress(state.progress, state.total, IDR.color.colorPrimaryDark)
    }

    private fun prepareSyncCompleteView() {
        binding.dashboardSyncCardProgress.visibility = View.VISIBLE
        binding.dashboardSyncCardProgressIndeterminateProgressBar.visibility = View.GONE

        binding.dashboardSyncCardProgressMessage.text =
            getString(IDR.string.dashboard_sync_card_complete)
        binding.dashboardSyncCardProgressMessage.setTextColor(context?.getColorStateList(IDR.color.simprints_green_dark))

        setProgress(100, 100, IDR.color.simprints_green_dark)
    }

    private fun updateLastSyncTime(lastSync: String?) {
        if (lastSync == null) {
            binding.dashboardSyncCardLastSync.visibility = View.GONE
        } else {
            binding.dashboardSyncCardLastSync.visibility = View.VISIBLE
            binding.dashboardSyncCardLastSync.text = String.format(
                getString(IDR.string.dashboard_card_sync_last_sync),
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

    private fun hideAllViews() {
        binding.dashboardSyncCardDefaultStateSyncButton.visibility = View.GONE
        binding.dashboardSyncCardFailedMessage.visibility = View.GONE
        binding.dashboardSyncCardSelectNoModules.visibility = View.GONE
        binding.dashboardSyncCardOffline.visibility = View.GONE
        binding.dashboardSyncCardProgress.visibility = View.GONE
        binding.dashboardSyncCardTryAgain.visibility = View.GONE
    }

    // I couldn't find a way to get from Android SDK the default text color (in line with the theme).
    // So I change a color for a TextView, then I can't set back to the default.
    // The card's title has always the same color - the default one.
    // Hacky way to extract the color from the title and use for the other TextViews
    private fun getDefaultGrayTextColor(): Int =
        binding.dashboardSyncCardTitle.textColors.defaultColor
}
