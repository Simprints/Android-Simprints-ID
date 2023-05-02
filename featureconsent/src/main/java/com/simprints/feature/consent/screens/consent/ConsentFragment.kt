package com.simprints.feature.consent.screens.consent

import android.graphics.Paint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.consent.R
import com.simprints.feature.consent.databinding.FragmentConsentBinding
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.toArgs
import com.simprints.infra.uibase.listeners.OnTabSelectedListener
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ConsentFragment : Fragment(R.layout.fragment_consent) {

    private val args by navArgs<ConsentFragmentArgs>()
    private val binding by viewBinding(FragmentConsentBinding::bind)
    private val viewModel by viewModels<ConsentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.consentPrivacyNotice.paintFlags = binding.consentPrivacyNotice.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.consentTextHolderView.movementMethod = ScrollingMovementMethod()

        handleClicks()
        observeState()

        findNavController().handleResult<ExitFormResult>(
            viewLifecycleOwner,
            R.id.consentFragment,
            ExitFormContract.DESTINATION_ID,
        ) { viewModel.handleExitFormResponse(it) }

        viewModel.loadConfiguration(args.consentType)
    }

    private fun handleClicks() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.declineClicked(getCurrentConsentTab())
        }

        binding.consentAcceptButton.setOnClickListener { viewModel.acceptClicked(getCurrentConsentTab()) }
        binding.consentDeclineButton.setOnClickListener { viewModel.declineClicked(getCurrentConsentTab()) }
        binding.consentPrivacyNotice.setOnClickListener { openPrivacyNotice() }
    }

    private fun observeState() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            if (state != null) updateUiWithState(state)
        }
        viewModel.showExitForm.observe(viewLifecycleOwner) { exitFormConfig ->
            exitFormConfig.getContentIfNotHandled()?.let {
                findNavController().navigate(R.id.action_consentFragment_to_refusalFragment, it.toArgs())
            }
        }
        viewModel.returnConsentResult.observe(viewLifecycleOwner) { isApproved ->
            isApproved.getContentIfNotHandled()?.let { findNavController().finishWithResult(this, it) }
        }
    }

    private fun openPrivacyNotice() {
        // TODO open privacy notice
    }

    private fun updateUiWithState(state: ConsentViewState) {
        binding.consentLogo.isVisible = state.showLogo
        // setup initial text to general consent
        binding.consentTextHolderView.text = state.consentText

        with(binding.consentTabHost) {
            // Fully reset tab state
            removeAllTabs()
            clearOnTabSelectedListeners()
            addTab(newTab().setText(IDR.string.consent_general_title), GENERAL_CONSENT_TAB)
            if (state.showParentalConsent) {
                addParentalConsentTab(state.consentText, state.parentalConsentText)
            }
        }
    }

    private fun TabLayout.addParentalConsentTab(generalConsentText: String, parentalConsentText: String) {
        addTab(newTab().setText(IDR.string.consent_parental_title), PARENTAL_CONSENT_TAB)
        addOnTabSelectedListener(OnTabSelectedListener { tab ->
            binding.consentTextHolderView.text = when (tab.position) {
                PARENTAL_CONSENT_TAB -> parentalConsentText
                else -> generalConsentText
            }
        })
    }

    private fun getCurrentConsentTab() = when (binding.consentTabHost.selectedTabPosition) {
        GENERAL_CONSENT_TAB -> ConsentTab.INDIVIDUAL
        PARENTAL_CONSENT_TAB -> ConsentTab.PARENTAL
        else -> throw IllegalStateException("Invalid consent tab selected")
    }

    companion object {
        private const val GENERAL_CONSENT_TAB = 0
        private const val PARENTAL_CONSENT_TAB = 1
    }
}
