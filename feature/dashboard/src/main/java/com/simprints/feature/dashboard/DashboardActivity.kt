package com.simprints.feature.dashboard

import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.storage.alert.ShowStorageAlertIfNecessaryUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : BaseActivity(R.layout.activity_dashboard_main) {
    @Inject
    lateinit var showStorageAlertIfNecessary: ShowStorageAlertIfNecessaryUseCase

    override fun onStart() {
        super.onStart()
        showStorageAlertIfNecessary()
    }
}
