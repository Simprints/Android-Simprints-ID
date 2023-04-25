package com.simprints.feature.dashboard.logout.sync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncBinding
import com.simprints.feature.dashboard.main.sync.SyncViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogoutSyncFragment : Fragment(R.layout.fragment_logout_sync) {

    private val viewModel by viewModels<SyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeLiveData()
    }

    private fun initViews() = with(binding) {
        logoutSyncCard.onSyncButtonClick = { viewModel.sync() }
        logoutSyncCard.onOfflineButtonClick =
            { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        logoutSyncCard.onSelectNoModulesButtonClick =
            { findNavController().navigate(R.id.action_mainFragment_to_moduleSelectionFragment) }
        logoutSyncToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncButton.setOnClickListener {
            findNavController().navigate(R.id.action_logoutSyncFragment_to_logoutSyncDeclineFragment)
        }
    }

    private fun observeLiveData() {
        viewModel.syncToBFSIDAllowed.observe(viewLifecycleOwner) {
            if (it) {
                binding.logoutSyncCard.visibility = View.VISIBLE
            } else {
                binding.logoutSyncCard.visibility = View.GONE
            }
        }
        viewModel.syncCardLiveData.observe(viewLifecycleOwner) {
            binding.logoutSyncCard.render(state = it)
        }
    }
}
