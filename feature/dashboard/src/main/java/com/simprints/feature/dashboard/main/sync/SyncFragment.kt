package com.simprints.feature.dashboard.main.sync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardSyncBinding
import com.simprints.feature.dashboard.requestlogin.LogoutReason
import com.simprints.feature.dashboard.requestlogin.RequestLoginFragmentArgs
import com.simprints.infra.resources.R as IDR
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SyncFragment : Fragment(R.layout.fragment_dashboard_card_sync) {

    private val viewModel by viewModels<SyncViewModel>()
    private val binding by viewBinding(FragmentDashboardCardSyncBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeLiveData()
    }

    private fun initViews() = with(binding.dashboardSyncCard) {
        onSyncButtonClick = { viewModel.sync() }
        onOfflineButtonClick = { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        onSelectNoModulesButtonClick =
            { findNavController().navigate(R.id.action_mainFragment_to_moduleSelectionFragment) }
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
            binding.dashboardSyncCard.render(state = it)
        }
        viewModel.signOutEventLiveData.observe(viewLifecycleOwner) {
            val logoutReason = LogoutReason(
                title = getString(IDR.string.dashboard_sync_project_ending_alert_title),
                body = getString(IDR.string.dashboard_sync_project_ending_message)
            )
            findNavController().navigate(
                R.id.action_mainFragment_to_requestLoginFragment,
                RequestLoginFragmentArgs(logoutReason = logoutReason).toBundle()
            )
        }
    }
}
