package com.simprints.id.activities.dashboard.cards.project

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.tools.AndroidResourcesHelper
import org.jetbrains.anko.layoutInflater

class DashboardProjectDetailsCardDisplayerImpl(
    private val context: Context,
    private val androidResourcesHelper: AndroidResourcesHelper
) : DashboardProjectDetailsCardDisplayer {

    private lateinit var root: LinearLayout
    private lateinit var card: View

    override fun initRoot(rootLayout: LinearLayout) {
        root = rootLayout
        card = inflateCard(root)
    }

    // TODO STOPSHIP use real values
    override fun displayProjectDetails() {
        with(card) {
            setTitle("World domination project")
            setCurrentUser("Pinky and The Brain")
            setScannerUsed("SP1234")
        }
    }

    private fun inflateCard(root: LinearLayout) = context.layoutInflater.inflate(
        R.layout.activity_dashboard_card_project_details, root, false
    )

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
