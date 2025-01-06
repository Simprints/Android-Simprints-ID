package com.simprints.feature.dashboard.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.BuildConfig
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentMainBinding
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainFragment : Fragment(R.layout.fragment_main) {
    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.dashboardToolbar.setOnMenuItemClickListener {
            menuItemClicked(it)
        }
        if (!BuildConfig.DEBUG) {
            binding.dashboardToolbar.menu.removeItem(R.id.debug)
        }
        viewModel.consentRequired.observe(viewLifecycleOwner) {
            binding.dashboardToolbar.menu
                .findItem(R.id.menuPrivacyNotice)
                .isVisible = it
        }
        viewModel.rootedDeviceDetected.observe(viewLifecycleOwner) {
            binding.rootedDeviceError.visibility = View.VISIBLE
        }
    }

    private fun menuItemClicked(item: MenuItem): Boolean = with(findNavController()) {
        when (item.itemId) {
            R.id.menuSettings -> navigateSafely(this@MainFragment, MainFragmentDirections.actionMainFragmentToSettingsFragment())
            R.id.debug -> navigateSafely(this@MainFragment, MainFragmentDirections.actionMainFragmentToDebugFragment())
            R.id.menuPrivacyNotice -> navigateSafely(this@MainFragment, MainFragmentDirections.actionMainFragmentToPrivacyNoticesFragment())
        }
        true
    }
}
