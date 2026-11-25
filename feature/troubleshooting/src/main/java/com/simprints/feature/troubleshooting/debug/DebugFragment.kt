package com.simprints.feature.troubleshooting.debug

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingDebugBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class DebugFragment : Fragment(R.layout.fragment_troubleshooting_debug) {
    private val viewModel by viewModels<DebugViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingDebugBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.troubleshootDebugClearFirebaseToken.setOnClickListener {
            viewModel.clearFirebaseToken()
            Toast.makeText(requireContext(), "Firebase token deleted", Toast.LENGTH_SHORT).show()
        }
        binding.troubleshootDebugCleanAll.setOnClickListener {
            viewModel.cleanAll()
            Toast.makeText(requireContext(), "Cleaning data...", Toast.LENGTH_SHORT).show()
        }
    }
}
