package com.simprints.feature.selectsubject.screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.view.ScannedCredentialDialog
import com.simprints.feature.selectsubject.R
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.selectsubject.databinding.FragmentSelectSubjectBinding
import com.simprints.feature.selectsubject.model.SelectSubjectState
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class SelectSubjectFragment : Fragment(R.layout.fragment_select_subject) {
    private val params: SelectSubjectParams by navigationParams()
    private val binding by viewBinding(FragmentSelectSubjectBinding::bind)
    private val viewModel by viewModels<SelectSubjectViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return viewModelFactory.create(params) as T
            }
        }
    }

    private var isAnimatingCompletion: Boolean = false
    private var pendingFinishAction: (() -> Unit)? = null
    private var progressAnimator: ObjectAnimator? = null

    @Inject
    lateinit var viewModelFactory: SelectSubjectViewModel.Factory
    private var dialog: BottomSheetDialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("SelectSubjectFragment started", tag = ORCHESTRATION)

        initObservers()
    }

    override fun onDestroyView() {
        clearAnimations()
        dismissDialog()
        super.onDestroyView()
    }

    private fun clearAnimations() {
        pendingFinishAction = null
        isAnimatingCompletion = false
        progressAnimator?.cancel()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SelectSubjectState.CredentialDialogDisplayed -> {
                    displayCredentialDialog(state.scannedCredential, state.displayedCredential)
                }

                SelectSubjectState.SavingExternalCredential -> renderSavingCredential()
                SelectSubjectState.SavingSubjectId -> renderSavingSubjectId()
            }
        }
        viewModel.finish.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let(::finishWithResult)
        }
    }

    private fun renderSavingCredential() = with(binding) {
        saveCredentialProgressContainer.isVisible = true
        confirmationSentContainer.isVisible = false
        startCredentialSaveAnimation()
    }

    private fun renderSavingSubjectId() = with(binding) {
        saveCredentialProgressContainer.isVisible = false
        confirmationSentContainer.isVisible = true
    }

    private fun displayCredentialDialog(scannedCredential: ScannedCredential, displayedCredential: TokenizableString.Raw) {
        dialog = ScannedCredentialDialog(
            context = requireActivity(),
            credential = scannedCredential,
            displayedCredential = displayedCredential,
            onConfirm = { viewModel.saveCredential(scannedCredential) },
            onSkip = (viewModel::finishWithoutSavingCredential)
        ).also(ScannedCredentialDialog::show)
    }

    private fun startCredentialSaveAnimation() = with(binding) {
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            this.duration = PROGRESS_BAR_DURATION_MS
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimatingCompletion = false
                    // Execute any pending action after the animation. Currently used is for next fragment navigation
                    pendingFinishAction?.invoke()
                    pendingFinishAction = null
                }
            })
        }
        progressAnimator?.start()
    }

    private fun finishWithResult(result: SelectSubjectResult) {
        findNavController().finishWithResult(this, result)
    }

    companion object {
        private const val PROGRESS_BAR_DURATION_MS = 1500L
    }
}
