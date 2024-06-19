package com.simprints.feature.login.screens.form

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.FragmentLoginFormBinding
import com.simprints.feature.login.databinding.ViewUrlChangeInputBinding
import com.simprints.feature.login.screens.form.SignInState.BackendMaintenanceError
import com.simprints.feature.login.screens.form.SignInState.BadCredentials
import com.simprints.feature.login.screens.form.SignInState.IntegrityException
import com.simprints.feature.login.screens.form.SignInState.IntegrityServiceTemporaryDown
import com.simprints.feature.login.screens.form.SignInState.MissingCredential
import com.simprints.feature.login.screens.form.SignInState.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.feature.login.screens.form.SignInState.Offline
import com.simprints.feature.login.screens.form.SignInState.ProjectIdMismatch
import com.simprints.feature.login.screens.form.SignInState.QrCameraUnavailable
import com.simprints.feature.login.screens.form.SignInState.QrCodeValid
import com.simprints.feature.login.screens.form.SignInState.QrGenericError
import com.simprints.feature.login.screens.form.SignInState.QrInvalidCode
import com.simprints.feature.login.screens.form.SignInState.QrNoCameraPermission
import com.simprints.feature.login.screens.form.SignInState.ShowUrlChangeDialog
import com.simprints.feature.login.screens.form.SignInState.Success
import com.simprints.feature.login.screens.form.SignInState.TechnicalFailure
import com.simprints.feature.login.screens.form.SignInState.Unknown
import com.simprints.feature.login.screens.qrscanner.QrScannerResult
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class LoginFormFragment : Fragment(R.layout.fragment_login_form) {

    private val args by navArgs<LoginFormFragmentArgs>()
    private val binding by viewBinding(FragmentLoginFormBinding::bind)
    private val viewModel by viewModels<LoginFormViewModel>()

    private lateinit var checkForPlayServicesResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    init {
        checkForPlayServicesResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                // Check again to make sure that the user did the needed actions.
                playServicesChecker.check(requireActivity(), checkForPlayServicesResultLauncher) {
                    finishWithError(it)
                }
            }
    }

    @Inject
    lateinit var playServicesChecker: GooglePlayServicesAvailabilityChecker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            finishWithError(LoginError.LoginNotCompleted)
        }

        findNavController().handleResult<QrScannerResult>(
            viewLifecycleOwner,
            R.id.loginFormFragment,
            R.id.loginQrScanner
        ) { viewModel.handleQrResult(args.loginParams.projectId, it) }

        initUi()
        observeUiState()
        playServicesChecker.check(requireActivity(), checkForPlayServicesResultLauncher) {
            finishWithError(it)
        }
    }

    private fun initUi() {
        binding.loginUserId.setText(args.loginParams.userId.value)
        binding.loginProjectId.setText(args.loginParams.projectId)

        binding.loginChangeUrlButton.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Change URL button clicked")
            viewModel.changeUrlClicked()
        }

        binding.loginButtonScanQr.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Scan QR button clicked")
            findNavController().navigateSafely(this, R.id.action_loginFormFragment_to_loginQrScanner)
        }
        binding.loginButtonSignIn.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Login button clicked")
            viewModel.signInClicked(
                args.loginParams,
                binding.loginProjectId.text.toString(),
                binding.loginProjectSecret.text.toString(),
            )
        }
    }

    private fun observeUiState() {
        viewModel.isProcessingSignIn.observe(viewLifecycleOwner) { isProcessingSignIn ->
            binding.loginProgress.isVisible = isProcessingSignIn
            binding.loginButtonScanQr.isEnabled = !isProcessingSignIn
            binding.loginButtonSignIn.isEnabled = !isProcessingSignIn
        }
        viewModel.signInState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let(::handleSignInResult)
        }
    }

    private fun handleSignInResult(result: SignInState) {
        binding.loginErrorCard.isVisible = false

        when (result) {
            // Showing toast
            MissingCredential -> showToast(IDR.string.login_missing_credentials_error)
            BadCredentials -> showToast(IDR.string.login_invalid_credentials_error)
            ProjectIdMismatch -> showToast(IDR.string.login_project_id_intent_mismatch_error)
            Offline -> showToast(IDR.string.login_no_network_error)
            IntegrityServiceTemporaryDown -> showToast(IDR.string.login_integrity_service_down_error)
            TechnicalFailure -> showToast(IDR.string.login_server_error)
            QrCameraUnavailable -> showToast(IDR.string.login_qr_code_scanning_camera_unavailable_error)
            QrGenericError -> showToast(IDR.string.login_qr_code_scanning_problem_error)
            QrInvalidCode -> showToast(IDR.string.login_invalid_qr_code_error)
            QrNoCameraPermission -> showToast(IDR.string.login_qr_code_scanning_camera_permission_error)

            is ShowUrlChangeDialog -> createChangeUrlDialog(result).show()

            // Showing error card
            is BackendMaintenanceError -> showOutageErrorCard(result.estimatedOutage)

            // Fill input fields
            is QrCodeValid -> updateFields(result)

            // Terminal cases
            Success -> finishWithSuccess()
            IntegrityException -> finishWithError(LoginError.IntegrityServiceError)
            MissingOrOutdatedGooglePlayStoreApp -> finishWithError(LoginError.MissingOrOutdatedPlayServices)
            Unknown -> finishWithError(LoginError.Unknown)
        }
    }

    private fun updateFields(result: QrCodeValid) {
        binding.loginProjectSecret.setText(result.projectSecret)
    }

    private fun showOutageErrorCard(estimatedOutage: String?) {
        binding.loginErrorText.text = estimatedOutage
            ?.let {
                getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    estimatedOutage
                )
            }
            ?: getString(IDR.string.error_backend_maintenance_message)
        binding.loginErrorCard.isVisible = true
    }

    private fun showToast(@StringRes messageId: Int) {
        Toast.makeText(requireContext(), getString(messageId), Toast.LENGTH_LONG).show()
    }

    private fun createChangeUrlDialog(result: ShowUrlChangeDialog): AlertDialog {
        val binding = ViewUrlChangeInputBinding.inflate(layoutInflater)
            .apply { loginUrlChangeInput.setText(result.currentUrl) }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(IDR.string.login_change_url)
            .setView(binding.root)
            .setNeutralButton(IDR.string.login_change_url_reset) { di, _ ->
                viewModel.saveNewUrl(null)
                di.dismiss()
            }
            .setPositiveButton(IDR.string.login_change_url_save) { di, _ ->
                viewModel.saveNewUrl(binding.loginUrlChangeInput.text.toString())
                di.dismiss()
            }
            .setNegativeButton(IDR.string.login_change_url_cancel) { di, _ -> di.dismiss() }
            .create()
    }

    private fun finishWithSuccess() {
        findNavController().finishWithResult(this, LoginResult(true))
    }

    private fun finishWithError(error: LoginError) {
        findNavController().finishWithResult(this, LoginResult(false, error))
    }

}
