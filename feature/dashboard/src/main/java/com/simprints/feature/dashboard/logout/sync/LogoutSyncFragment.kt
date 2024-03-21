package com.simprints.feature.dashboard.logout.sync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncBinding
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.feature.dashboard.main.sync.SyncViewModel
import com.simprints.feature.dashboard.views.SyncCardState
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogoutSyncFragment : Fragment(R.layout.fragment_logout_sync) {

    private val logoutSyncViewModel by viewModels<LogoutSyncViewModel>()
    private val syncViewModel by viewModels<SyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeLiveData()
    }

    private fun initViews() = with(binding) {
        logoutSyncCard.onSyncButtonClick = { syncViewModel.sync() }
        logoutSyncCard.onOfflineButtonClick =
            { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        logoutSyncCard.onSelectNoModulesButtonClick =
            { findNavController().navigateSafely(this@LogoutSyncFragment, R.id.action_logoutSyncFragment_to_moduleSelectionFragment) }
        logoutSyncToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncButton.setOnClickListener {
            findNavController().navigateSafely(this@LogoutSyncFragment, R.id.action_logoutSyncFragment_to_logoutSyncDeclineFragment)
        }
        logoutButton.setOnClickListener {
            logoutSyncViewModel.logout()
            findNavController().navigateSafely(this@LogoutSyncFragment, R.id.action_logoutSyncFragment_to_requestLoginFragment)
        }
    }

    private fun observeLiveData() = with(binding) {
        syncViewModel.syncCardLiveData.observe(viewLifecycleOwner) { state ->
            val isLogoutButtonVisible = isLogoutButtonVisible(state)
            logoutSyncCard.render(state)
            logoutButton.isVisible = isLogoutButtonVisible
            logoutWithoutSyncButton.isVisible = isLogoutButtonVisible.not()
            logoutSyncInfo.isInvisible = isLogoutButtonVisible
        }
    }

    /**
     * Helper function that calculates whether the 'proceed to log out' button should be visible.
     * The button should be visible only when synchronization is complete
     */
    private fun isLogoutButtonVisible(state: SyncCardState) =
        state is SyncCardState.SyncComplete
}
