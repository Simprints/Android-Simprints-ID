package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.DashboardSyncCardViewModel
import com.simprints.id.tools.utils.AndroidResourcesHelper
import org.jetbrains.anko.runOnUiThread
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncButton: Button = rootView.findViewById(R.id.dashboardSyncCardSyncButton)
    private val totalPeopleInLocal: TextView = rootView.findViewById(R.id.totalPeopleInLocal)

    @Inject
    lateinit var androidResourcesHelper: AndroidResourcesHelper

    private var syncButtonEnabled = true

    init {
        (rootView.context.applicationContext as Application).component.inject(this)
    }

    override fun bind(viewModel: ViewModel) {
        val cardViewModel = viewModel as? DashboardSyncCardViewModel
        cardViewModel?.let {
            it.stateLiveData.observe(this, Observer<DashboardSyncCardViewModel.State> { state ->
                rootView.context.runOnUiThread {
                    with(state) {
                        setTotalPeopleInDbCounter(peopleInDb)
                        setUploadCounter(peopleToUpload)
                        setListenerForSyncButton(onSyncActionClicked)
                        setSyncButtonState(showSyncButton, isDownSyncRunning)
                        setDownloadCounter(peopleToDownload)
                        setLastSyncTime(lastSyncTime)
                    }
                }
            })
        }
    }

    private fun setSyncButtonState(showSyncButton:Boolean, isSyncRunning: Boolean) {
        if (isSyncRunning) {
            disableSyncButtonIfEnabled()
        } else {
            enableSyncButtonIfDisabled()
        }

        if(showSyncButton) {
            syncButton.visibility = View.VISIBLE
        } else {
            syncButton.visibility = View.INVISIBLE
        }
    }


    private fun setTotalPeopleInDbCounter(peopleInDb: Int) {
        totalPeopleInLocal.text = "${Math.max(peopleInDb, 0)}"
    }

    private fun setUploadCounter(peopleToUpload: Int) {
        syncUploadCount.text = "${Math.max(peopleToUpload, 0)}"
    }

    private fun setListenerForSyncButton(onActionClicked: () -> Unit) {
        syncButton.setOnClickListener { onActionClicked() }
    }

    private fun setLastSyncTime(lastSyncTime: String) {
        syncDescription.text = lastSyncTime
    }

    private fun setDownloadCounter(peopleToDownload: Int?) {
        syncDownloadCount.text = peopleToDownload?.let { "${Math.max(it, 0)}" } ?: ""
    }

    private fun disableSyncButtonIfEnabled() {
        if (syncButtonEnabled) {
            syncButtonEnabled = false
            syncButton.background = androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners_disabled)
            syncButton.text = androidResourcesHelper.getString(R.string.syncing)
        }
    }

    private fun enableSyncButtonIfDisabled() {
        if (!syncButtonEnabled) {
            syncButtonEnabled = true
            syncButton.background = androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners)
            syncButton.text = androidResourcesHelper.getString(R.string.dashboard_card_sync_now)
        }
    }
}
