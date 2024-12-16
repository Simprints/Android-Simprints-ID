package com.simprints.feature.dashboard.main.dailyactivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardDailyActivityBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class DailyActivityFragment : Fragment(R.layout.fragment_dashboard_card_daily_activity) {
    private val viewModel by viewModels<DailyActivityViewModel>()
    private val binding by viewBinding(FragmentDashboardCardDailyActivityBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dailyActivity.observe(viewLifecycleOwner) {
            updateCard(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun updateCard(dailyActivityState: DashboardDailyActivityState) {
        if (dailyActivityState.hasNoActivity()) {
            binding.dashboardDailyActivityCard.visibility = View.GONE
        } else {
            binding.dashboardDailyActivityCard.visibility = View.VISIBLE
            displayDailyActivity(dailyActivityState)
        }
    }

    private fun displayDailyActivity(dailyActivityState: DashboardDailyActivityState) {
        binding.dashboardDailyActivityCardTitle.text = String.format(
            getString(IDR.string.dashboard_activity_card_activity),
            viewModel.getCurrentDateAsString(),
        )
        setEnrolmentsCount(dailyActivityState.enrolments)
        setIdentificationsCount(dailyActivityState.identifications)
        setVerificationsCount(dailyActivityState.verifications)
        setDividers(dailyActivityState)
    }

    private fun setEnrolmentsCount(count: Int) {
        if (count > 0) {
            binding.groupEnrolments.visibility = View.VISIBLE
            binding.dashboardDailyActivityCardEnrolmentsCount.text = count.toString()
            binding.enrolmentsLabel.text = resources.getQuantityString(
                IDR.plurals.dashboard_activity_card_enrolments,
                count,
            )
        } else {
            binding.groupEnrolments.visibility = View.GONE
        }
    }

    private fun setIdentificationsCount(count: Int) {
        if (count > 0) {
            binding.groupIdentifications.visibility = View.VISIBLE
            binding.dashboardDailyActivityCardIdentificationsCount.text = count.toString()
            binding.identificationsLabel.text = resources.getQuantityString(
                IDR.plurals.dashboard_activity_card_identifications,
                count,
            )
        } else {
            binding.groupIdentifications.visibility = View.GONE
        }
    }

    private fun setVerificationsCount(count: Int) {
        if (count > 0) {
            binding.groupVerifications.visibility = View.VISIBLE
            binding.dashboardDailyActivityCardVerificationsCount.text = count.toString()
            binding.verificationsLabel.text = resources.getQuantityString(
                IDR.plurals.dashboard_activity_card_verifications,
                count,
            )
        } else {
            binding.groupVerifications.visibility = View.GONE
        }
    }

    private fun setDividers(dailyActivityState: DashboardDailyActivityState) {
        val shouldShowEnrolmentsDivider = dailyActivityState.hasEnrolments() &&
            (
                dailyActivityState.hasIdentifications() ||
                    dailyActivityState.hasVerifications()
            )
        binding.dividerEnrolments.visibility = if (shouldShowEnrolmentsDivider) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val shouldShowIdentificationsDivider = dailyActivityState.hasIdentifications() &&
            dailyActivityState.hasVerifications()

        binding.dividerIdentifications.visibility = if (shouldShowIdentificationsDivider) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
