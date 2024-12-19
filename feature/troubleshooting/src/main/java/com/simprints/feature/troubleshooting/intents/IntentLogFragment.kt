package com.simprints.feature.troubleshooting.intents

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.TroubleshootingFragmentDirections
import com.simprints.feature.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingListBinding
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
internal class IntentLogFragment : Fragment(R.layout.fragment_troubleshooting_list) {
    private val viewModel by viewModels<IntentLogViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingListBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.logs.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter = TroubleshootingListAdapter(it) {
                findNavController().navigateSafely(this, openEventsList(it))
            }
        }
        viewModel.collectData()
    }

    private fun openEventsList(string: String): NavDirections = TroubleshootingFragmentDirections
        .actionTroubleshootingFragmentToTroubleshootingEventLogFragment(string)
}
