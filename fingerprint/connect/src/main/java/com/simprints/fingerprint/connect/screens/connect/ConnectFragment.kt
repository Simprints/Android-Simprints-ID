package com.simprints.fingerprint.connect.screens.connect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentConnectBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.infra.uibase.viewbinding.viewBinding

internal class ConnectFragment : Fragment(R.layout.fragment_connect) {
    private val binding by viewBinding(FragmentConnectBinding::bind)
    private val viewModel: ConnectScannerViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            binding.connectTitle.setText(step.messageRes)
            binding.connectProgress.progress = step.progress
        }
    }
}
