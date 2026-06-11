package com.simprints.feature.validatepool.screen

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.validatepool.R
import com.simprints.feature.validatepool.ValidateSubjectPoolFragmentParams
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.feature.validatepool.databinding.FragmentValidateSubjectPoolBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ValidateSubjectPoolFragment : Fragment(R.layout.fragment_validate_subject_pool) {
    private val viewModel: ValidateSubjectPoolViewModel by viewModels()
    private val binding by viewBinding(FragmentValidateSubjectPoolBinding::bind)
    private val params: ValidateSubjectPoolFragmentParams by navigationParams()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ValidateSubjectPoolFragment started", tag = ORCHESTRATION)

        viewModel.state.observe(viewLifecycleOwner, LiveDataEventWithContentObserver(::renderState))

        viewModel.lastSyncLabel.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { label ->
                binding.validationIssueLastSynced.text = getString(IDR.string.id_pool_validation_last_sync, label)
            },
        )

        binding.validationActionsClose.setOnClickListener { finishWithResult(false) }
        binding.validationActionsContinue.setOnClickListener { finishWithResult(true) }
        binding.validationActionsSync.setOnClickListener { viewModel.startSync(params.enrolmentRecordQuery, params.mode) }

        viewModel.checkIdentificationPool(params.enrolmentRecordQuery, params.mode)
    }

    private fun renderState(state: ValidateSubjectPoolState) = when (state) {
        ValidateSubjectPoolState.Success -> finishWithResult(true)

        ValidateSubjectPoolState.Validating -> setViews(
            showValidating = true,
        )

        ValidateSubjectPoolState.AttendantMismatch -> setViews(
            descriptionRes = IDR.string.id_pool_validation_user_mismatch_message,
        )

        ValidateSubjectPoolState.ModuleMismatch -> setViews(
            descriptionRes = IDR.string.id_pool_validation_module_mismatch_message,
        )

        ValidateSubjectPoolState.RequiresSync -> setViews(
            descriptionRes = IDR.string.id_pool_validation_sync_required_message,
            showSyncBlock = true,
        )

        ValidateSubjectPoolState.SyncInProgress -> setViews(
            showProgress = true,
            showSyncBlock = true,
        )

        ValidateSubjectPoolState.PoolEmpty -> setViews(
            descriptionRes = IDR.string.id_pool_validation_pool_empty_message,
            showSyncBlock = true,
        )
    }

    private fun setViews(
        @StringRes descriptionRes: Int? = null,
        showValidating: Boolean = false,
        showProgress: Boolean = false,
        showSyncBlock: Boolean = false,
    ) = with(binding) {
        validationValidating.isVisible = showValidating
        validationMainCard.isVisible = !showValidating
        validationActions.isVisible = !showValidating

        descriptionRes?.let { validationIssueDescription.setText(it) }

        // Last synced label
        validationIssueLastSynced.isVisible = showSyncBlock

        // Loading
        validationLoadingBlock.isVisible = showSyncBlock
        validationActionsSync.isVisible = showSyncBlock && !showProgress
        validationLoadingIndicator.isVisible = showProgress && showSyncBlock
        validationLoadingIndicatorText.isVisible = showProgress && showSyncBlock
    }

    private fun finishWithResult(isValid: Boolean) {
        findNavController().finishWithResult(this, ValidateSubjectPoolResult(isValid))
    }
}
