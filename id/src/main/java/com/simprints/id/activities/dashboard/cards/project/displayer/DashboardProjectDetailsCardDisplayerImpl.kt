package com.simprints.id.activities.dashboard.cards.project.displayer

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.id.tools.AndroidResourcesHelper

class DashboardProjectDetailsCardDisplayerImpl(
    private val androidResourcesHelper: AndroidResourcesHelper
) : DashboardProjectDetailsCardDisplayer {

    private lateinit var root: LinearLayout

    override fun initRoot(rootLayout: LinearLayout) {
        root = rootLayout
    }

    override fun displayProjectDetails(projectDetails: DashboardProjectState) {
        with(root) {
            setTitle(projectDetails.title)
            setCurrentUser(projectDetails.lastUser)
            setScannerUsed(projectDetails.lastScanner)
        }
    }

    private fun View.setTitle(title: String) {
        findViewById<TextView>(R.id.dashboard_project_details_card_title).text = title
    }

    private fun View.setCurrentUser(currentUser: String) {
        findViewById<TextView>(
            R.id.dashboard_project_details_card_current_user
        ).text = androidResourcesHelper.getString(
            R.string.dashboard_card_current_user, arrayOf(currentUser)
        )
    }

    private fun View.setScannerUsed(scannerUsed: String) {
        findViewById<TextView>(
            R.id.dashboard_project_details_card_scanner_used
        ).text = androidResourcesHelper.getString(
            R.string.dashboard_card_scanner_used, arrayOf(scannerUsed)
        )
    }

}
