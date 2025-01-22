package com.simprints.feature.validatepool.screen

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.validatepool.R
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.feature.validatepool.databinding.FragmentValidateSubjectPoolBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ValidateSubjectPoolFragment : Fragment(R.layout.fragment_validate_subject_pool) {
    private val viewModel: ValidateSubjectPoolViewModel by viewModels()
    private val binding by viewBinding(FragmentValidateSubjectPoolBinding::bind)
    private val args: ValidateSubjectPoolFragmentArgs by navArgs()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("ValidateSubjectPoolFragment started", tag = ORCHESTRATION)

        viewModel.state.observe(viewLifecycleOwner, LiveDataEventWithContentObserver(::renderState))

        binding.validationActionsClose.setOnClickListener { finishWithResult(false) }
        binding.validationActionsContinue.setOnClickListener { finishWithResult(true) }
        binding.validationActionsSync.setOnClickListener { viewModel.syncAndRetry(args.subjectQuery) }

        viewModel.checkIdentificationPool(args.subjectQuery)
    }

    private fun renderState(state: ValidateSubjectPoolState) = when (state) {
        ValidateSubjectPoolState.Success -> finishWithResult(true)
        ValidateSubjectPoolState.Validating -> setViews(
            titleRes = IDR.string.id_pool_validation_default_message,
        )

        ValidateSubjectPoolState.UserMismatch -> setViews(
            titleRes = IDR.string.id_pool_validation_user_mismatch_message,
            showTitle = true,
            showCloseAction = true,
        )

        ValidateSubjectPoolState.ModuleMismatch -> setViews(
            titleRes = IDR.string.id_pool_validation_module_mismatch_message,
            showTitle = true,
            showCloseAction = true,
        )

        ValidateSubjectPoolState.RequiresSync -> setViews(
            titleRes = IDR.string.id_pool_validation_sync_required_message,
            showTitle = true,
            showCloseAction = true,
            showSyncAction = true,
        )

        ValidateSubjectPoolState.SyncInProgress -> setViews(
            titleRes = IDR.string.id_pool_validation_syncing_message,
            showTitle = true,
            showProgress = true,
        )

        ValidateSubjectPoolState.PoolEmpty -> setViews(
            titleRes = IDR.string.id_pool_validation_pool_empty_message,
            showTitle = true,
            showCloseAction = true,
        )
    }

    private fun setViews(
        @StringRes titleRes: Int,
        showTitle: Boolean = false,
        showProgress: Boolean = false,
        showCloseAction: Boolean = false,
        showSyncAction: Boolean = false,
    ) = with(binding) {
        validationIssueMessage.setText(titleRes)
        validationIssueTitle.isVisible = showTitle
        validationLoadingIndicator.isVisible = showProgress
        validationActions.isVisible = showCloseAction || showSyncAction
        validationActionsSync.isVisible = showSyncAction
    }

    private fun finishWithResult(isValid: Boolean) {
        findNavController().finishWithResult(this, ValidateSubjectPoolResult(isValid))
    }
}
