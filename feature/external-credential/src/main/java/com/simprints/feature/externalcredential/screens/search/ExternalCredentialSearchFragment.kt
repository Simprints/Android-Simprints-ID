package com.simprints.feature.externalcredential.screens.search

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderCredentialCard(state)
            renderSearchProgress(state.searchState)
            renderButtons(state)
        }
    }

    private fun renderCredentialCard(state: SearchCredentialState) {
        renderImage(state.scannedCredential)
    }

    private fun renderSearchProgress(searchState: SearchState) {
    }

    private fun renderButtons(state: SearchCredentialState) {
    }

    private fun renderImage(scannedCredential: ScannedCredential) {
        val imagePath: String? = scannedCredential.previewImagePath
        val boundingBox: BoundingBox? = scannedCredential.imageBoundingBox
        binding.documentPreview.isVisible = imagePath != null
        if (imagePath == null) return

        try {
            BitmapFactory.decodeFile(imagePath)?.let { bitmap ->
                val finalBitmap = if (boundingBox != null) {
                    zoomOntoCredentialUseCase(bitmap, boundingBox)
                } else bitmap
                binding.documentPreview.setImageBitmap(finalBitmap)
            }
        } catch (e: Exception) {
            Simber.e("Unable to get [$imagePath] OCR image", e, tag = MULTI_FACTOR_ID)
        }

    }

}
