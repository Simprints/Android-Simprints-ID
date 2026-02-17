package com.simprints.feature.externalcredential.screens.search

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSearchBinding
import com.simprints.feature.externalcredential.ext.getCredentialFieldTitle
import com.simprints.feature.externalcredential.ext.getCredentialTypeString
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ZoomOntoCredentialUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExternalCredentialSearchFragment : Fragment(R.layout.fragment_external_credential_search) {
    private val args: ExternalCredentialSearchFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentExternalCredentialSearchBinding::bind)
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialSearchViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return viewModelFactory.create(
                    scannedCredential = args.scannedCredential,
                    externalCredentialParams = mainViewModel.params,
                ) as T
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ExternalCredentialSearchViewModel.Factory

    @Inject
    lateinit var zoomOntoCredentialUseCase: ZoomOntoCredentialUseCase
    private var credentialTextWatcher: TextWatcher? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderCredentialCard(state)
            renderSearchProgress(state.searchState, state.scannedCredential.credentialType, state.flowType)
            renderButtons(state)
        }
        viewModel.finishEvent.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { result ->
                mainViewModel.finish(result)
            },
        )
    }

    private fun renderCredentialCard(state: SearchCredentialState) = with(binding) {
        val credential = state.displayedCredential?.value.orEmpty()
        val credentialType = state.scannedCredential.credentialType
        val credentialField = resources.getCredentialFieldTitle(credentialType)
        val currentEditTextValue = credentialEditText.text.toString()
        val isEditingCredential = state.isEditingCredential
        renderImage(state.scannedCredential)
        renderCredentialEdit(state)
        credential.takeIf { currentEditTextValue.isEmpty() }?.let {
            credentialEditText.setText(it) // Setting only once at the start
        }
        documentTypeTitle.text = credentialField
        credentialEditText.inputType = viewModel.getKeyBoardInputType()
        credentialEditText.hint = credentialField
        credentialValue.text = currentEditTextValue
        confirmCredentialCheckbox.isVisible = state.searchState != SearchState.Searching
        confirmCredentialCheckbox.text = getString(IDR.string.mfid_confirmation_checkbox_text, credentialField)
        confirmCredentialCheckbox.isChecked = state.isConfirmed && !state.isEditingCredential
        confirmCredentialCheckbox.isEnabled = !state.isEditingCredential

        iconEditCredential.setOnClickListener {
            if (isEditingCredential) {
                viewModel.confirmCredentialUpdate(updatedCredential = credentialEditText.text.toString().asTokenizableRaw())
            }
            viewModel.updateIsEditingCredential(isEditing = !isEditingCredential)
        }
        confirmCredentialCheckbox.setOnCheckedChangeListener { _, checkedId ->
            viewModel.updateConfirmation(isConfirmed = checkedId)
        }

        credentialTextWatcher?.let(credentialEditText::removeTextChangedListener)
        credentialTextWatcher = credentialEditText.addTextChangedListener(
            afterTextChanged = { _ ->
                renderEditIcon(isEditingCredential)
            },
        )
    }

    private fun renderSearchProgress(
        searchState: SearchState,
        credentialType: ExternalCredentialType,
        flowType: FlowType,
    ) = with(binding) {
        when (searchState) {
            SearchState.Searching -> {
                searchProgressCard.isVisible = true
                searchResultCard.isVisible = false
            }

            is SearchState.CredentialLinked -> {
                searchProgressCard.isVisible = false
                searchResultCard.isVisible = true

                when (flowType) {
                    FlowType.ENROL -> renderEnrolCredentialLinked(credentialType)
                    else -> { // Identification flow
                        if (searchState.hasSuccessfulVerifications) {
                            renderIdentifyCredentialVerificationConfirmed()
                        } else {
                            renderIdentifyCredentialVerificationFailed(credentialType)
                        }
                    }
                }
            }

            SearchState.CredentialNotFound -> {
                searchProgressCard.isVisible = false
                when (flowType) {
                    FlowType.ENROL -> renderEnrolCredentialNotFound()
                    else -> renderIdentifyCredentialNotFound(credentialType)
                }
            }
        }
    }

    private fun renderEnrolCredentialLinked(credentialType: ExternalCredentialType) = with(binding) {
        iconSearchResult.setImageResource(R.drawable.ic_warning)
        iconSearchResult.isVisible = true
        val credentialField = resources.getCredentialTypeString(credentialType)
        val searchResultText = getString(IDR.string.mfid_search_found_enrol, credentialField)
        textSearchResult.text = searchResultText
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_red))
    }

    private fun renderIdentifyCredentialVerificationConfirmed() = with(binding) {
        iconSearchResult.setImageResource(IDR.drawable.ic_checked_green_large)
        iconSearchResult.isVisible = true
        textSearchResult.setText(IDR.string.mfid_search_found_identification)
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
    }

    private fun renderIdentifyCredentialVerificationFailed(credentialType: ExternalCredentialType) = with(binding) {
        iconSearchResult.setImageResource(R.drawable.ic_warning)
        iconSearchResult.isVisible = true
        val credential = resources.getCredentialTypeString(credentialType)
        textSearchResult.text = getString(IDR.string.mfid_search_found_identification_low_match_score, credential)
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_red))
    }

    private fun renderEnrolCredentialNotFound() = with(binding) {
        searchResultCard.isVisible = false
    }

    private fun renderIdentifyCredentialNotFound(credentialType: ExternalCredentialType) = with(binding) {
        searchResultCard.isVisible = true
        iconSearchResult.isVisible = false
        val credential = resources.getCredentialTypeString(credentialType)
        val credentialField = resources.getCredentialFieldTitle(credentialType)
        val searchResultText = getString(IDR.string.mfid_search_not_found_identification, credentialField, credential)
        textSearchResult.text = searchResultText
        val textColor = IDR.color.simprints_black
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }

    private fun renderButtons(state: SearchCredentialState) = with(binding) {
        val isSearching = state.searchState != SearchState.Searching
        buttonRecapture.isVisible = isSearching
        buttonConfirm.isVisible = isSearching
        buttonConfirm.isEnabled = state.isConfirmed && !state.isEditingCredential
        viewModel.getButtonTextResource(state.searchState, state.flowType)?.run(buttonConfirm::setText)
        buttonConfirm.setOnClickListener {
            viewModel.finish(state)
        }
        buttonRecapture.setOnClickListener {
            viewModel.trackRecapture()
            findNavController().navigateSafely(
                this@ExternalCredentialSearchFragment,
                ExternalCredentialSearchFragmentDirections.actionExternalCredentialSearchToExternalCredentialSelectFragment(),
            )
        }
    }

    private fun renderImage(credential: ScannedCredential) {
        val documentImagePath: String? = credential.documentImagePath
        binding.documentPreview.isVisible = documentImagePath != null
        if (documentImagePath == null) return

        try {
            val imagePath: String = credential.zoomedCredentialImagePath ?: documentImagePath
            val displayedImage = BitmapFactory.decodeFile(imagePath)
            binding.documentPreview.setImageBitmap(displayedImage)
        } catch (e: Exception) {
            Simber.e("Unable to get [$documentImagePath] OCR image", e, tag = MULTI_FACTOR_ID)
        }
    }

    private fun renderCredentialEdit(state: SearchCredentialState) = with(binding) {
        val isEditingCredential = state.isEditingCredential
        renderEditIcon(isEditingCredential)
        val iconRes = if (isEditingCredential) {
            R.drawable.ic_done
        } else {
            R.drawable.ic_edit
        }
        iconEditCredential.setImageResource(iconRes)
        credentialValue.isVisible = !isEditingCredential
        credentialEditText.isVisible = isEditingCredential
        if (isEditingCredential) {
            credentialEditText.setSelection(credentialEditText.text.length)
            val inputManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(credentialEditText, InputMethodManager.SHOW_IMPLICIT)
        }
        if (!isEditingCredential) {
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        requireActivity().hideKeyboard()
    }

    private fun renderEditIcon(isEditingCredential: Boolean) = with(binding) {
        val isEditIconEnabled = if (isEditingCredential) {
            viewModel.isCredentialFormatValid(credentialEditText.text?.toString())
        } else {
            true
        }
        iconEditCredential.alpha = if (isEditIconEnabled) 1.0f else 0.5f
        iconEditCredential.isEnabled = isEditIconEnabled
    }
}
