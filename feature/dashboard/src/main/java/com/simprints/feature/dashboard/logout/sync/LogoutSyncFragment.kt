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
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class LogoutSyncFragment : Fragment(R.layout.fragment_logout_sync) {
    private val viewModel by viewModels<LogoutSyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        initViews()
        observeState()
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

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: LogoutSyncUiState) {
        // null: no value yet, the button and the info text keep their XML defaults.
        val isLogoutWithoutSyncVisible = state.isLogoutWithoutSyncVisible ?: return
        val visibility = if (isLogoutWithoutSyncVisible) View.VISIBLE else View.INVISIBLE
        binding.logoutSyncInfo.visibility = visibility
        binding.logoutWithoutSyncButton.visibility = visibility
    }

    // LogoutSyncUiEffect has no effects; collected anyway so every screen has the same wiring
    @Suppress("UNUSED_PARAMETER")
    private fun handleEffect(effect: LogoutSyncUiEffect) = Unit
}
