package com.simprints.feature.dashboard.logout.sync

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncBinding
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogoutSyncFragment : Fragment(R.layout.fragment_logout_sync) {
    private val viewModel by viewModels<LogoutSyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeLiveData()
    }

    private fun initViews() = with(binding) {
        logoutSyncToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncButton.setOnClickListener {
            findNavController().navigateSafely(
                this@LogoutSyncFragment,
                LogoutSyncFragmentDirections.actionLogoutSyncFragmentToLogoutSyncDeclineFragment(),
            )
        }
    }

    private fun observeLiveData() = with(binding) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isLogoutWithoutSyncVisibleLiveData.observe(viewLifecycleOwner) { isLogoutWithoutSyncVisible ->
                    logoutSyncInfo.visibility = if (isLogoutWithoutSyncVisible) View.VISIBLE else View.INVISIBLE
                    logoutWithoutSyncButton.visibility = if (isLogoutWithoutSyncVisible) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        viewModel.logoutEventLiveData.observe(viewLifecycleOwner) {
            findNavController().navigateSafely(
                this@LogoutSyncFragment,
                R.id.action_logoutSyncFragment_to_requestLoginFragment,
            )
        }
    }

}
