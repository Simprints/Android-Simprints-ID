package com.simprints.feature.exitform.screen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.extentions.setTextWithFallbacks
import com.simprints.core.tools.extentions.showToast
import com.simprints.core.tools.extentions.textWatcherOnChange
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.R
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.databinding.FragmentExitFormBinding
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExitFormFragment : Fragment(R.layout.fragment_exit_form) {

    private val args by navArgs<ExitFormFragmentArgs>()
    private val viewModel by viewModels<ExitFormViewModel>()
    private val binding by viewBinding(FragmentExitFormBinding::bind)

    private val textWatcher = textWatcherOnChange {
        viewModel.reasonTextChanged(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val config = args.exitFormConfiguration

        binding.exitFormTitle.setTextWithFallbacks(
            rawText = config.title,
            textFallback = config.titleRes,
            default = IDR.string.why_did_you_skip_biometrics,
        )
        binding.exitFormGoBack.setTextWithFallbacks(
            rawText = config.backButton,
            textFallback = config.backButtonRes,
            default = IDR.string.exit_form_return_to_simprints,
        )

        setLayoutChangeListener()
        setOptionsVisible(config.visibleOptions)
        handleClicks()
        observeViewModel()
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListener() {
        binding.exitFormScrollView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.exitFormScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setOptionsVisible(options: Set<ExitFormOption>) = with(binding) {
        exitFormRadioReligiousConcerns.isVisible = options.contains(ExitFormOption.ReligiousConcerns)
        exitFormRadioDataConcerns.isVisible = options.contains(ExitFormOption.DataConcerns)
        exitFormRadioNoPermission.isVisible = options.contains(ExitFormOption.NoPermission)
        exitFormRadioAppNotWorking.isVisible = options.contains(ExitFormOption.AppNotWorking)
        exitFormRadioScannerNotWorking.isVisible = options.contains(ExitFormOption.ScannerNotWorking)
        exitFromRadioPersonNotPresent.isVisible = options.contains(ExitFormOption.PersonNotPresent)
        exitFormRadioTooYoung.isVisible = options.contains(ExitFormOption.TooYoung)
        exitFormRadioOther.isVisible = options.contains(ExitFormOption.Other)
    }

    private fun handleClicks() {
        binding.exitFormRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.optionSelected(viewIdToOption(checkedId))

            // Input field is disabled until first option is selected avoid it
            // stealing focus and to minimise the confusion
            binding.exitFormInputField.isEnabled = true
        }
        binding.exitFormGoBack.setOnClickListener {
            findNavController().finishWithResult(this, ExitFormResult(false))
        }
        binding.exitFormSubmit.setOnClickListener {
            viewModel.submitClicked(binding.exitFormInputField.text.toString())
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.handleBackButton()
        }
    }

    private fun viewIdToOption(viewId: Int) = when (viewId) {
        R.id.exitFormRadioReligiousConcerns -> ExitFormOption.ReligiousConcerns
        R.id.exitFormRadioDataConcerns -> ExitFormOption.DataConcerns
        R.id.exitFormRadioNoPermission -> ExitFormOption.NoPermission
        R.id.exitFormRadioAppNotWorking -> ExitFormOption.AppNotWorking
        R.id.exitFormRadioScannerNotWorking -> ExitFormOption.ScannerNotWorking
        R.id.exitFromRadioPersonNotPresent -> ExitFormOption.PersonNotPresent
        R.id.exitFormRadioTooYoung -> ExitFormOption.TooYoung
        else -> ExitFormOption.Other
    }

    private fun observeViewModel() {
        viewModel.requestReasonEvent.observe(viewLifecycleOwner) {
            setFocusOnExitReasonAndDisableSubmit()
        }
        viewModel.requestSelectOptionEvent.observe(viewLifecycleOwner) {
            requireContext().showToast(getString(IDR.string.refusal_toast_select_option_submit))
        }
        viewModel.requestFormSubmitEvent.observe(viewLifecycleOwner) {
            requireContext().showToast(getString(IDR.string.refusal_toast_submit))
        }
        viewModel.submitEnabled.observe(viewLifecycleOwner) {
            binding.exitFormSubmit.isEnabled = true == it
        }
        viewModel.finishEvent.observe(viewLifecycleOwner) {
            val (answer, reason) = it.peekContent()
            findNavController().finishWithResult(this,
                ExitFormResult(true, answer, reason)
            )
        }
    }

    private fun setFocusOnExitReasonAndDisableSubmit() = with(binding.exitFormInputField) {
        requestFocus()
        addTextChangedListener(textWatcher)

        val inputManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

}
