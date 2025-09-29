package com.simprints.feature.externalcredential.screens.search

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.R
import com.simprints.infra.resources.R as IDR
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSearchBinding
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.feature.externalcredential.screens.search.usecase.ZoomOntoCredentialUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
internal class ExternalCredentialSearchFragment : Fragment(R.layout.fragment_external_credential_search) {
    private val args: ExternalCredentialSearchFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentExternalCredentialSearchBinding::bind)
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialSearchViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return viewModelFactory.create(args.scannedCredential, mainViewModel.flowType) as T
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

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderCredentialCard(state)
            renderSearchProgress(state.searchState, state.scannedCredential.credentialType, state.flowType)
            renderButtons(state)
        }
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

            is SearchState.SubjectFound -> {
                searchProgressCard.isVisible = false
                searchResultCard.isVisible = true

                val iconRes = when (flowType) {
                    FlowType.ENROL -> R.drawable.ic_warning
                    else -> {
                        if (searchState.isVerificationSuccessful) {
                            R.drawable.ic_done
                        } else {
                            R.drawable.ic_warning
                        }
                    }
                }
                iconSearchResult.setImageResource(iconRes)

                val credential = getString(mainViewModel.mapTypeToStringResource(credentialType))
                val credentialField = getString(mainViewModel.mapTypeToCredentialFieldResource(credentialType))
                val searchResultText = when (flowType) {
                    FlowType.ENROL -> getString(IDR.string.mfid_search_found_enrol, credentialField)
                    else -> {
                        if (searchState.isVerificationSuccessful) {
                            getString(IDR.string.mfid_search_found_identification)
                        } else {
                            getString(IDR.string.mfid_search_found_identification_low_match_score, credential)
                        }
                    }
                }
                textSearchResult.text = searchResultText

                val textColor = when (flowType) {
                    FlowType.ENROL -> IDR.color.simprints_red
                    else -> {
                        if (searchState.isVerificationSuccessful) {
                            IDR.color.simprints_black
                        } else {
                            IDR.color.simprints_red
                        }
                    }
                }
                textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            }

            SearchState.SubjectNotFound -> {
                // no search result card is shown during Enrol, refer to designs. This section only processes style for Identification flow
                searchProgressCard.isVisible = false
                searchResultCard.isVisible = flowType == FlowType.IDENTIFY
                iconSearchResult.isVisible = false
                val credential = getString(mainViewModel.mapTypeToStringResource(credentialType))
                val credentialField = getString(mainViewModel.mapTypeToCredentialFieldResource(credentialType))
                val searchResultText = getString(IDR.string.mfid_search_not_found_identification, credentialField, credential)
                textSearchResult.text = searchResultText
                val textColor = IDR.color.simprints_black
                textSearchResult.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            }
        }
    }

    private fun renderButtons(state: SearchCredentialState) = with(binding) {
        val isVisible = state.searchState != SearchState.Searching
        buttonRecapture.isVisible = isVisible
        buttonConfirm.isVisible = isVisible
        buttonConfirm.isEnabled = state.isConfirmed
        viewModel.getButtonTextResource(state.searchState, state.flowType)?.run(buttonConfirm::setText)
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
    }
}
