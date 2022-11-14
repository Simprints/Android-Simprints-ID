package com.simprints.feature.dashboard.projectdetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardProjectDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ProjectDetailsFragment : Fragment(R.layout.fragment_dashboard_card_project_details) {

    private val viewModel by viewModels<ProjectDetailsViewModel>()
    private val binding by viewBinding(FragmentDashboardCardProjectDetailsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.projectCardStateLiveData.observe(viewLifecycleOwner) {
            binding.dashboardProjectDetailsCardTitle.text = it.title
            binding.dashboardProjectDetailsCardCurrentUser.text = String.format(
                view.context.getString(IDR.string.dashboard_card_current_user),
                it.lastUser
            )
            with(binding.dashboardProjectDetailsCardScannerUsed) {
                if (it.lastScanner.isEmpty()) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    text = String.format(
                        context.getString(IDR.string.dashboard_card_scanner_used),
                        it.lastScanner
                    )
                }
            }
        }
    }
}
