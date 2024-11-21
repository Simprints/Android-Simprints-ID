package com.simprints.feature.dashboard.settings.troubleshooting.overview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentTroubleshootingOverviewBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverviewFragment : Fragment(R.layout.fragment_troubleshooting_overview) {

    private val viewModel by viewModels<OverviewViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingOverviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.projectIds.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewIds.text = it.orEmpty()
        }
        viewModel.licenseStates.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewLicences.text = it.ifBlank { "No licenses found" }
        }
        viewModel.networkStates.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewNetwork.text = it.orEmpty()
        }

        viewModel.collectData()
    }

}
