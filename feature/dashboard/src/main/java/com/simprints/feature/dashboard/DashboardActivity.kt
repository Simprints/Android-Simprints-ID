package com.simprints.feature.dashboard

import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.dashboard.requestlogin.LogoutReason
import com.simprints.feature.dashboard.requestlogin.RequestLoginFragmentArgs
import com.simprints.feature.storage.alert.ShowStorageAlertIfNecessaryUseCase
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class DashboardActivity : BaseActivity(R.layout.activity_dashboard_main) {
    @Inject
    lateinit var showStorageAlertIfNecessary: ShowStorageAlertIfNecessaryUseCase

    override fun onStart() {
        super.onStart()
        showStorageAlertIfNecessary()
    }

    override fun onLogout(isProjectEnded: Boolean) {
        val logoutReason = if (isProjectEnded) {
            LogoutReason(
                title = getString(IDR.string.dashboard_sync_project_ending_alert_title),
                body = getString(IDR.string.dashboard_sync_project_ending_message),
            )
        } else {
            null
        }
        try {
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_to_requestLoginFragment,
                RequestLoginFragmentArgs(logoutReason = logoutReason).toBundle(),
            )
        } catch (t: IllegalArgumentException) {
            Simber.i("Already on login screen or navigation graph not ready", t)
        }
    }
}
