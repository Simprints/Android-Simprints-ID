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
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.R
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.databinding.FragmentExitFormBinding
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.listeners.TextWatcherOnChangeListener
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.view.setTextWithFallbacks
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExitFormFragment : Fragment(R.layout.fragment_exit_form) {

    private val args by navArgs<ExitFormFragmentArgs>()
    private val viewModel by viewModels<ExitFormViewModel>()
    private val binding by viewBinding(FragmentExitFormBinding::bind)

    private val textWatcher = TextWatcherOnChangeListener {
        viewModel.reasonTextChanged(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val config = args.exitFormConfiguration

        binding.exitFormTitle.setTextWithFallbacks(
            rawText = config.title,
            textFallback = config.titleRes,
            default = IDR.string.exit_form_title_biometrics,
        )
        binding.exitFormGoBack.setTextWithFallbacks(
            rawText = config.backButton,
            textFallback = config.backButtonRes,
            default = IDR.string.exit_form_continue_default_button,
        )

        setOptionsVisible(config.visibleOptions)
        handleClicks()
        observeViewModel()
    }

    private fun setOptionsVisible(options: Set<ExitFormOption>) = with(binding) {
        exitFormRadioReligiousConcerns.isVisible = options.contains(ExitFormOption.ReligiousConcerns)
        exitFormRadioDataConcerns.isVisible = options.contains(ExitFormOption.DataConcerns)
        exitFormRadioNoPermission.isVisible = options.contains(ExitFormOption.NoPermission)
        exitFormRadioAppNotWorking.isVisible = options.contains(ExitFormOption.AppNotWorking)
        exitFormRadioScannerNotWorking.isVisible = options.contains(ExitFormOption.ScannerNotWorking)
        exitFromRadioPersonNotPresent.isVisible = options.contains(ExitFormOption.PersonNotPresent)
        exitFormRadioTooYoung.isVisible = options.contains(ExitFormOption.TooYoung)
        exitFormRadioWrongAgeGroupSelected.isVisible = options.contains(ExitFormOption.WrongAgeGroupSelected)
        exitFormUncooperativeChild.isVisible = options.contains(ExitFormOption.UncooperativeChild)
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
        R.id.exitFormUncooperativeChild -> ExitFormOption.UncooperativeChild
        R.id.exitFormRadioWrongAgeGroupSelected -> ExitFormOption.WrongAgeGroupSelected
        else -> ExitFormOption.Other
    }

    private fun observeViewModel() {
        viewModel.requestReasonEvent.observe(viewLifecycleOwner) {
            setFocusOnExitReasonAndDisableSubmit()
        }
        viewModel.requestSelectOptionEvent.observe(viewLifecycleOwner) {
            requireContext().showToast(IDR.string.exit_form_select_option_submit_warning)
        }
        viewModel.requestFormSubmitEvent.observe(viewLifecycleOwner) {
            requireContext().showToast(IDR.string.exit_form_submit_warning)
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
