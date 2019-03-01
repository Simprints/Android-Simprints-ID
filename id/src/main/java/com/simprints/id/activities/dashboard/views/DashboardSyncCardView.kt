package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModelState
import com.simprints.id.activities.dashboard.viewModels.syncCard.SyncCardState
import com.simprints.id.activities.dashboard.viewModels.syncCard.SyncCardState.*
import com.simprints.id.tools.utils.AndroidResourcesHelper
import org.jetbrains.anko.runOnUiThread
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncButton: Button = rootView.findViewById(R.id.dashboardSyncCardSyncButton)
    private val totalPeopleInLocal: TextView = rootView.findViewById(R.id.dashboardCardSyncTotalLocalText)

    @Inject
    lateinit var androidResourcesHelper: AndroidResourcesHelper

    init {
        (rootView.context.applicationContext as Application).component.inject(this)
    }

    override fun bind(viewModel: ViewModel) {
        val cardViewModel = viewModel as? DashboardSyncCardViewModel
        cardViewModel?.let {
            it.viewModelStateLiveData.observe(this, Observer<DashboardSyncCardViewModelState> { state ->
                rootView.context.runOnUiThread {
                    with(state) {
                        setTotalPeopleInDbCounter(peopleInDb)
                        setUploadCounter(peopleToUpload)
                        setListenerForSyncButton(onSyncActionClicked)
                        setSyncButtonState(syncCardState)
                        setDownloadCounter(peopleToDownload)
                        setLastSyncTime(lastSyncTime)
                    }
                }
            })
        }
    }

    private fun setSyncButtonState(showSyncCard: SyncCardState) {
        when(showSyncCard) {
            SYNC_RUNNING -> setSyncButtonStyleForRunning()
            SYNC_CALCULATING -> setSyncButtonStyleForCalculating()
            SYNC_DISABLED -> setSyncButtonStyleForDisabled()
            SYNC_ENABLED -> setSyncButtonStyleForEnabled()
        }
    }


    private fun setTotalPeopleInDbCounter(peopleInDb: Int?) {
        totalPeopleInLocal.text = peopleInDb?.let { "${Math.max(it, 0)}" } ?: ""
    }

    private fun setUploadCounter(peopleToUpload: Int?) {
        syncUploadCount.text = peopleToUpload?.let { "${Math.max(it, 0)}" } ?: ""
    }

    private fun setListenerForSyncButton(onActionClicked: () -> Unit) {
        syncButton.setOnClickListener { onActionClicked() }
    }

    private fun setLastSyncTime(lastSyncTime: String) {
        syncDescription.text = String.format(androidResourcesHelper.getString(R.string.dashboard_card_sync_last_sync), lastSyncTime)
    }

    private fun setDownloadCounter(peopleToDownload: Int?) {
        syncDownloadCount.text = peopleToDownload?.let { "${Math.max(it, 0)}" } ?: ""
    }

    private fun setSyncButtonStyleForDisabled() {
        syncButton.isEnabled = false
        syncButton.visibility = View.INVISIBLE
    }

    private fun setSyncButtonStyleForEnabled() {
        syncButton.isEnabled = true
        setSyncButtonStyle(androidResourcesHelper.getString(R.string.dashboard_card_sync_now), androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners))
    }

    private fun setSyncButtonStyleForRunning() {
        syncButton.isEnabled = false
        setSyncButtonStyle(androidResourcesHelper.getString(R.string.dashboard_card_syncing), androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners_disabled))
    }

    private fun setSyncButtonStyleForCalculating() {
        syncButton.isEnabled = false
        setSyncButtonStyle(androidResourcesHelper.getString(R.string.dashboard_card_calculating), androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners_disabled))
    }

    private fun setSyncButtonStyle(text: String, background: Drawable?) {
        syncButton.background = background
        syncButton.text = text
        syncButton.visibility = View.VISIBLE
    }
}
