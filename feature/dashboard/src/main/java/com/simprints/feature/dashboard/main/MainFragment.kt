package com.simprints.feature.dashboard.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentMainBinding
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class MainFragment : Fragment(R.layout.fragment_main) {
    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        binding.dashboardToolbar.setOnMenuItemClickListener {
            menuItemClicked(it)
        }
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    // MainUiEffect has no effects; collected anyway so every screen has wiring
    @Suppress("UNUSED_PARAMETER")
    private fun handleEffect(effect: MainUiEffect) = Unit

    private fun render(state: MainUiState) {
        // null: not loaded yet, the privacy notice menu item keeps its XML default (visible).
        state.isConsentRequired?.let { isConsentRequired ->
            binding.dashboardToolbar.menu
                .findItem(R.id.menuPrivacyNotice)
                .isVisible = isConsentRequired
        }
        binding.rootedDeviceError.isVisible = state.isRootedDeviceDetected
    }

    private fun menuItemClicked(item: MenuItem): Boolean = with(findNavController()) {
        when (item.itemId) {
            R.id.menuSettings -> navigateSafely(this@MainFragment, MainFragmentDirections.actionMainFragmentToSettingsFragment())
            R.id.menuPrivacyNotice -> navigateSafely(this@MainFragment, MainFragmentDirections.actionMainFragmentToPrivacyNoticesFragment())
        }
        true
    }
}
