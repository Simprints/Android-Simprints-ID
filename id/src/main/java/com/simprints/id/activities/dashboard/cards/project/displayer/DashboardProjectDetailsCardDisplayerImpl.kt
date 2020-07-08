package com.simprints.id.activities.dashboard.cards.project.displayer

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState

class DashboardProjectDetailsCardDisplayerImpl: DashboardProjectDetailsCardDisplayer {

    private lateinit var root: LinearLayout

    override fun initRoot(rootLayout: LinearLayout) {
        root = rootLayout
    }

    override fun displayProjectDetails(projectDetails: DashboardProjectState) {
        with(root) {
            setTitle(projectDetails.title)
            setCurrentUser(projectDetails.lastUser, context)
            setScannerUsed(projectDetails.lastScanner, context)
        }
    }

    private fun View.setTitle(title: String) {
        findViewById<TextView>(R.id.dashboard_project_details_card_title).text = title
    }

    private fun View.setCurrentUser(currentUser: String, ctx: Context) {
        findViewById<TextView>(
            R.id.dashboard_project_details_card_current_user
        ).text = String.format(ctx.getString(
            R.string.dashboard_card_current_user), currentUser)
    }

    private fun View.setScannerUsed(scannerUsed: String, ctx: Context) {
        with(findViewById<TextView>(
            R.id.dashboard_project_details_card_scanner_used
        )) {
            if (scannerUsed.isEmpty()) {
                this.visibility = GONE
            } else {
                this.visibility = VISIBLE
                text = String.format(ctx.getString(
                    R.string.dashboard_card_scanner_used), scannerUsed)
            }
        }
    }

}
