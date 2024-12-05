package com.simprints.feature.dashboard.settings.troubleshooting.events

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentTroubleshootingListBinding
import com.simprints.feature.dashboard.settings.troubleshooting.TroubleshootingFragmentDirections
import com.simprints.feature.dashboard.settings.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EventScopeLogFragment : Fragment(R.layout.fragment_troubleshooting_list) {

    private val viewModel by viewModels<EventsLogViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingListBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scopes.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter =
                TroubleshootingListAdapter(it) { openEventsList(it) }
        }
        viewModel.collectEventScopes()
    }

    private fun openEventsList(scopeId: String) {
        findNavController().navigate(
            TroubleshootingFragmentDirections
                .actionTroubleshootingFragmentToTroubleshootingEventLogFragment(scopeId)
        )
    }
}
