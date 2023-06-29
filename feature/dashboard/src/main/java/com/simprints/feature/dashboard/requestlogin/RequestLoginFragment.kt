package com.simprints.feature.dashboard.requestlogin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentRequestLoginBinding
import com.simprints.infra.authstore.AuthStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class RequestLoginFragment : Fragment(R.layout.fragment_request_login) {

    private val binding by viewBinding(FragmentRequestLoginBinding::bind)
    private val args: RequestLoginFragmentArgs by navArgs()

    @PackageVersionName
    @Inject
    lateinit var packageVersionName: String

    @DeviceID
    @Inject
    lateinit var deviceId: String

    @Inject
    lateinit var authStore: AuthStore

    private var wasLogoutReasonDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wasLogoutReasonDisplayed = savedInstanceState?.getBoolean(KEY_WAS_LOGOUT_REASON_DISPLAYED) ?: false
        binding.tvDeviceId.text = getString(IDR.string.device_id, deviceId)
        binding.simprintsIdVersionTextView.text =
            String.format(getString(IDR.string.front_simprintsId_version), packageVersionName)
        args.logoutReason?.takeIf { !wasLogoutReasonDisplayed }?.run(::displayLogoutReasonDialog)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_WAS_LOGOUT_REASON_DISPLAYED, wasLogoutReasonDisplayed)
    }

    override fun onResume() {
        super.onResume()
        if (authStore.signedInProjectId.isNotEmpty() && authStore.signedInUserId.isNotEmpty())
            findNavController().navigate(R.id.action_requestLoginFragment_to_mainFragment)
    }

    private fun displayLogoutReasonDialog(logoutReason: LogoutReason) {
        wasLogoutReasonDisplayed = true
        AlertDialog.Builder(requireContext())
            .setTitle(logoutReason.title)
            .setMessage(logoutReason.body)
            .setPositiveButton(
                getString(com.simprints.infra.resources.R.string.close)
            ) { di, _ -> di.dismiss() }.create()
            .show()
    }

    companion object {
        private const val KEY_WAS_LOGOUT_REASON_DISPLAYED = "KEY_LOGOUT_REASON"
    }
}
