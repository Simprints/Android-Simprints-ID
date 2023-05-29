package com.simprints.feature.login.screens.form

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.FragmentLoginFormBinding
import com.simprints.feature.login.screens.form.SignInState.BackendMaintenanceError
import com.simprints.feature.login.screens.form.SignInState.BadCredentials
import com.simprints.feature.login.screens.form.SignInState.IntegrityException
import com.simprints.feature.login.screens.form.SignInState.IntegrityServiceTemporaryDown
import com.simprints.feature.login.screens.form.SignInState.MissingCredential
import com.simprints.feature.login.screens.form.SignInState.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.feature.login.screens.form.SignInState.Offline
import com.simprints.feature.login.screens.form.SignInState.ProjectIdMismatch
import com.simprints.feature.login.screens.form.SignInState.Success
import com.simprints.feature.login.screens.form.SignInState.TechnicalFailure
import com.simprints.feature.login.screens.form.SignInState.Unknown
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker.Companion.GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class LoginFormFragment : Fragment(R.layout.fragment_login_form) {

    private val args by navArgs<LoginFormFragmentArgs>()
    private val binding by viewBinding(FragmentLoginFormBinding::bind)
    private val viewModel by viewModels<LoginFormViewModel>()

    @Inject
    lateinit var playServicesChecker: GooglePlayServicesAvailabilityChecker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            finishWithError(LoginError.LoginNotCompleted)
        }

        initUi()
        observeUiState()
        viewModel.init()
        playServicesChecker.check(requireActivity()) { finishWithError(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE) {
            // Check again to make sure that the user did the need actions.
            playServicesChecker.check(requireActivity()) { finishWithError(it) }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUi() {
        binding.loginUserId.setText(args.loginParams.userId)
        binding.loginButtonScanQr.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Scan QR button clicked")
            // TODO open QR scanner screen for result
        }
        binding.loginButtonSignIn.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Login button clicked")

            binding.loginProgress.isVisible = true
            viewModel.signInClicked(
                args.loginParams,
                binding.loginProjectId.text.toString(),
                binding.loginProjectSecret.text.toString(),
            )
        }
    }

    private fun observeUiState() {
        viewModel.signInState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let(::handleSignInResult)
        }
    }

    private fun handleSignInResult(result: SignInState) {
        binding.loginProgress.isVisible = false
        binding.loginErrorCard.isVisible = false

        when (result) {
            // Showing toast
            MissingCredential -> showToast(IDR.string.login_missing_credentials)
            BadCredentials -> showToast(IDR.string.login_invalid_credentials)
            ProjectIdMismatch -> showToast(IDR.string.login_project_id_intent_mismatch)
            Offline -> showToast(IDR.string.login_no_network)
            IntegrityServiceTemporaryDown -> showToast(IDR.string.integrity_service_down)
            TechnicalFailure -> showToast(IDR.string.login_server_error)

            // Showing error card
            is BackendMaintenanceError -> showOutageErrorCard(result.estimatedOutage)

            // Terminal cases
            Success -> finishWithSuccess()
            IntegrityException -> finishWithError(LoginError.IntegrityServiceError)
            MissingOrOutdatedGooglePlayStoreApp -> finishWithError(LoginError.MissingOrOutdatedPlayServices)
            Unknown -> finishWithError(LoginError.Unknown)
        }
    }

    private fun showOutageErrorCard(estimatedOutage: String?) {
        binding.loginErrorText.text = estimatedOutage
            ?.let { getString(IDR.string.error_backend_maintenance_with_time_message, estimatedOutage) }
            ?: getString(IDR.string.error_backend_maintenance_message)
        binding.loginErrorCard.isVisible = true
    }

    private fun showToast(@StringRes messageId: Int) {
        Toast.makeText(requireContext(), getString(messageId), Toast.LENGTH_LONG).show()
    }

    private fun finishWithSuccess() {
        findNavController().finishWithResult(this, LoginResult(true))
    }

    private fun finishWithError(error: LoginError) {
        findNavController().finishWithResult(this, LoginResult(false, error))
    }

}
