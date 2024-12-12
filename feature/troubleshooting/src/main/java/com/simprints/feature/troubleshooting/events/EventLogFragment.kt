package com.simprints.feature.troubleshooting.events

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingStandaloneListBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EventLogFragment : Fragment(R.layout.fragment_troubleshooting_standalone_list) {
    private val args by navArgs<EventLogFragmentArgs>()

    private val viewModel by viewModels<EventsLogViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingStandaloneListBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.troubleshootingToolbar.title = args.scopeId
        binding.troubleshootingToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.events.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter = TroubleshootingListAdapter(it)
        }
        viewModel.collectEvents(args.scopeId)
    }
}
