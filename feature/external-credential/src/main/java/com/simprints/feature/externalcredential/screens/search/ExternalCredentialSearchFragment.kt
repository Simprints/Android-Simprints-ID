package com.simprints.feature.externalcredential.screens.search

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSearchBinding
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.feature.externalcredential.screens.search.usecase.ZoomOntoCredentialUseCase
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
                    externalCredentialParams = mainViewModel.params
                ) as T
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ExternalCredentialSearchViewModel.Factory


    @Inject
    lateinit var zoomOntoCredentialUseCase: ZoomOntoCredentialUseCase
    private var isEditingCredential: Boolean = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            }
        )
    }

    private fun renderCredentialCard(state: SearchCredentialState) = with(binding) {
        val credential = state.scannedCredential.credential
        val credentialType = state.scannedCredential.credentialType
        val credentialField = getString(mainViewModel.mapTypeToCredentialFieldResource(credentialType))
        renderImage(state.scannedCredential)
        documentTypeTitle.text = credentialField
        credentialValue.text = credential
        credentialEditText.inputType = viewModel.getKeyBoardInputType()
        credentialEditText.setText(credential)
        credentialEditText.hint = credentialField
        confirmCredentialCheckbox.isVisible = state.searchState != SearchState.Searching
        confirmCredentialCheckbox.text = getString(IDR.string.mfid_confirmation_checkbox_text, credentialField)
        confirmCredentialCheckbox.isChecked = state.isConfirmed

        iconEditCredential.setOnClickListener {
            viewModel.updateConfirmation(isConfirmed = false)
            toggleCredentialEdit()
            if (!isEditingCredential) {
                viewModel.updateCredentialValue(credentialEditText.text.toString())
            }
        }
        confirmCredentialCheckbox.setOnCheckedChangeListener { _, checkedId ->
            viewModel.updateConfirmation(isConfirmed = checkedId)
        }
    }

    private fun renderSearchProgress(searchState: SearchState, credentialType: ExternalCredentialType, flowType: FlowType) = with(binding) {
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
                            renderIdentifyCredentialVerificationConfirmed(credentialType)
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
        val credentialField = getString(mainViewModel.mapTypeToCredentialFieldResource(credentialType))
        val searchResultText = getString(IDR.string.mfid_search_found_enrol, credentialField)
        textSearchResult.text = searchResultText
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_red))
    }

    private fun renderIdentifyCredentialVerificationConfirmed(credentialType: ExternalCredentialType) = with(binding) {
        iconSearchResult.setImageResource(IDR.drawable.ic_checked_green_large)
        iconSearchResult.isVisible = true
        textSearchResult.setText(IDR.string.mfid_search_found_identification)
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
    }

    private fun renderIdentifyCredentialVerificationFailed(credentialType: ExternalCredentialType) = with(binding) {
        iconSearchResult.setImageResource(R.drawable.ic_warning)
        iconSearchResult.isVisible = true
        val credential = getString(mainViewModel.mapTypeToStringResource(credentialType))
        textSearchResult.text = getString(IDR.string.mfid_search_found_identification_low_match_score, credential)
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_red))
    }

    private fun renderEnrolCredentialNotFound() = with(binding) {
        searchResultCard.isVisible = false
    }

    private fun renderIdentifyCredentialNotFound(credentialType: ExternalCredentialType) = with(binding) {
        searchResultCard.isVisible = true
        iconSearchResult.isVisible = false
        val credential = getString(mainViewModel.mapTypeToStringResource(credentialType))
        val credentialField = getString(mainViewModel.mapTypeToCredentialFieldResource(credentialType))
        val searchResultText = getString(IDR.string.mfid_search_not_found_identification, credentialField, credential)
        textSearchResult.text = searchResultText
        val textColor = IDR.color.simprints_black
        textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }

    private fun renderButtons(state: SearchCredentialState) = with(binding) {
        val isSearching = state.searchState != SearchState.Searching
        buttonRecapture.isVisible = isSearching
        buttonConfirm.isVisible = isSearching
        buttonConfirm.isEnabled = state.isConfirmed
        viewModel.getButtonTextResource(state.searchState, state.flowType)?.run(buttonConfirm::setText)
        buttonConfirm.setOnClickListener {
            viewModel.finish(state.searchState)
        }
        buttonRecapture.setOnClickListener {
            findNavController().navigateSafely(
                this@ExternalCredentialSearchFragment,
                ExternalCredentialSearchFragmentDirections.actionExternalCredentialSearchToExternalCredentialSelectFragment()
            )
        }
    }

    private fun renderImage(scannedCredential: ScannedCredential) {
        val imagePath: String? = scannedCredential.previewImagePath
        val boundingBox: BoundingBox? = scannedCredential.imageBoundingBox
        binding.documentPreview.isVisible = imagePath != null
        if (imagePath == null) return

        try {
            val fullImage = BitmapFactory.decodeFile(imagePath)
            val finalBitmap = if (boundingBox != null) {
                zoomOntoCredentialUseCase(fullImage, boundingBox)
            } else fullImage
            binding.documentPreview.setImageBitmap(finalBitmap)
        } catch (e: Exception) {
            Simber.e("Unable to get [$imagePath] OCR image", e, tag = MULTI_FACTOR_ID)
        }

    }

    private fun toggleCredentialEdit() = with(binding) {
        isEditingCredential = !isEditingCredential
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
}
