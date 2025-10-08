package com.simprints.feature.externalcredential.screens.search

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSearchBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ExternalCredentialSearchFragment : Fragment(R.layout.fragment_external_credential_search) {
    private val args: ExternalCredentialSearchFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentExternalCredentialSearchBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        val imagePath = args.scannedCredential.previewImagePath!!
        loadScannedImage(imagePath)
    }

    private fun loadScannedImage(imagePath: String) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        binding.scannedImage.setImageBitmap(bitmap)
    }
}
