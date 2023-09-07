package com.simprints.feature.orchestrator.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.feature.clientapi.R
import com.simprints.feature.clientapi.databinding.FragmentStubBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.infra.resources.R as IDR

/**
 * To be used as temporary destination for unfinished flows.
 *
 * TODO: Remove once orchestrator is done - https://simprints.atlassian.net/browse/CORE-2845
 */
class StubFragment : Fragment(R.layout.fragment_stub) {

    private val args by navArgs<StubFragmentArgs>()
    private val binding by viewBinding(FragmentStubBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stubTitle.text = args.message ?: getString(IDR.string.app_name)
    }
}
