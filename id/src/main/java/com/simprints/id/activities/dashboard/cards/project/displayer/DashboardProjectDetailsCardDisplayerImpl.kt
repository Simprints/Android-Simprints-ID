package com.simprints.id.activities.dashboard.cards.project.displayer

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.infra.resources.R as IDR

class DashboardProjectDetailsCardDisplayerImpl: DashboardProjectDetailsCardDisplayer {

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
        ).text = String.format(context.getString(
            IDR.string.dashboard_card_current_user), currentUser)
    }

    private fun View.setScannerUsed(scannerUsed: String) {
        with(findViewById<TextView>(
            R.id.dashboard_project_details_card_scanner_used
        )) {
            if (scannerUsed.isEmpty()) {
                this.visibility = GONE
            } else {
                this.visibility = VISIBLE
                text = String.format(context.getString(
                    IDR.string.dashboard_card_scanner_used), scannerUsed)
            }
        }
    }

}
