package com.simprints.feature.externalcredential.screens.skip

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSkipBinding
import com.simprints.feature.externalcredential.ext.getCredentialTypeString
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class ExternalCredentialSkipFragment : Fragment(R.layout.fragment_external_credential_skip) {
    private val binding by viewBinding(FragmentExternalCredentialSkipBinding::bind)
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
    }

    private fun initObservers() {
        mainViewModel.externalCredentialTypes.observe(viewLifecycleOwner) { credentialTypes ->
            initViews(credentialTypes)
            initListeners()
        }
    }

    private fun initViews(credentialTypes: List<ExternalCredentialType>) = with(binding) {
        val dynamicTextReasonItemMap =
            mapOf(
                title to IDR.string.mfid_skip_title,
                skipReasonDoesNotHaveDocument to IDR.string.mfid_skip_reason_does_not_have,
                skipReasonDidNotBring to IDR.string.mfid_skip_reason_did_not_bring,
                skipReasonIncorrect to IDR.string.mfid_skip_reason_incorrect,
                skipReasonDoesNotWantToProvide to IDR.string.mfid_skip_reason_does_not_want_to_provide,
                skipReasonDamaged to IDR.string.mfid_skip_reason_damaged,
                skipReasonUnableToScan to IDR.string.mfid_skip_reason_unable_to_scan,
            )
        dynamicTextReasonItemMap.forEach { entry ->
            val textView = entry.key
            val stringRes = entry.value
            val credentialText = when (credentialTypes.size) {
                1 -> resources.getCredentialTypeString(credentialTypes.first())
                else -> getString(IDR.string.mfid_type_any_document)
            }
            textView.text = getString(stringRes, credentialText)
        }
    }

    private fun initListeners() = with(binding) {
        skipCredentialScanRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            reasonTextInputLayout.isVisible = checkedId == R.id.skipReasonOther

            val isSkipButtonEnabled = when (checkedId) {
                R.id.skipReasonOther -> {
                    reasonTextInput.text.toString().isNotEmpty()
                }

                else -> true
            }
            buttonSkip.isEnabled = isSkipButtonEnabled
        }
        reasonTextInput.addTextChangedListener(
            afterTextChanged = {
                buttonSkip.isEnabled = it.toString().isNotEmpty()
            },
        )
        buttonGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
        buttonSkip.setOnClickListener {
            mainViewModel.finish(
                ExternalCredentialSearchResult(
                    flowType = mainViewModel.params.flowType,
                    scannedCredential = null,
                    matchResults = emptyList(),
                ),
            )
        }
    }
}
