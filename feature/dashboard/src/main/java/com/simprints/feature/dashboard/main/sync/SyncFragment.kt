package com.simprints.feature.dashboard.main.sync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardSyncBinding
import com.simprints.feature.dashboard.main.MainFragmentDirections
import com.simprints.feature.dashboard.requestlogin.LogoutReason
import com.simprints.feature.dashboard.requestlogin.RequestLoginFragmentArgs
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SyncFragment : Fragment(R.layout.fragment_dashboard_card_sync) {
    private val viewModel by viewModels<SyncViewModel>()
    private val binding by viewBinding(FragmentDashboardCardSyncBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeLiveData()

        findNavController().handleResult<LoginResult>(
            viewLifecycleOwner,
            R.id.mainFragment,
            LoginContract.DESTINATION,
        ) { result -> viewModel.handleLoginResult(result) }
    }

    private fun initViews() = with(binding.dashboardSyncCard) {
        onSyncButtonClick = { viewModel.sync() }
        onOfflineButtonClick = { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        onSelectNoModulesButtonClick = {
            findNavController().navigateSafely(
                parentFragment,
                MainFragmentDirections.actionMainFragmentToModuleSelectionFragment(),
            )
        }
        onLoginButtonClick = { viewModel.login() }
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
                body = getString(IDR.string.dashboard_sync_project_ending_message),
            )
            findNavController().navigateSafely(
                parentFragment,
                R.id.action_mainFragment_to_requestLoginFragment,
                RequestLoginFragmentArgs(logoutReason = logoutReason).toBundle(),
            )
        }
        viewModel.loginRequestedEventLiveData.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { loginArgs ->
                findNavController().navigateSafely(
                    parentFragment,
                    R.id.action_mainFragment_to_login,
                    loginArgs,
                )
            },
        )
    }
}
